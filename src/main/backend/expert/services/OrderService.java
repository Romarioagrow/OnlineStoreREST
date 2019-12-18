package expert.services;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import expert.domain.OriginalProduct;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import expert.domain.Order;
import expert.domain.Product;
import expert.domain.User;
import expert.dto.OrderedProduct;
import expert.repos.OrderRepo;
import expert.repos.ProductRepo;
import expert.repos.UserRepo;
import java.util.*;

@Log
@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepo orderRepo;
    private final UserRepo userRepo;
    private final ProductRepo productRepo;

    public boolean acceptOrder(Map<String, String> orderDetails) {
        log.info(orderDetails.toString());

        Long orderID = Long.parseLong(orderDetails.get("orderID"));
        Order order = orderRepo.findByOrderID(orderID);

        order.setClientName(concatClientName(orderDetails));
        order.setClientMobile(orderDetails.get("mobile"));

        if (orderDetails.get("discountAmount") != null) {
            int discountPrice  = Integer.parseInt(orderDetails.get("discountPrice"));
            int discountAmount = Integer.parseInt(orderDetails.get("discountAmount"));
            order.setDiscountPrice(discountPrice);
            order.setDiscount(discountAmount);
            User user = order.getUser();
            user.setBonus(user.getBonus() - discountAmount);
            userRepo.save(user);
        }

        order.getOrderedProducts().forEach((productID, amount) -> {
            Product product = productRepo.findByProductID(productID);
            order.getOrderedList().add(new OrderedProduct(productID, amount, product.getFullName(), product.getPic(), product.getFinalPrice()));
        });

        order.setAccepted(true);
        orderRepo.save(order);
        return true;
    }

    private String concatClientName(Map<String, String> orderDetails) {
        if (orderDetails.get("patronymic") == null) {
            return orderDetails.get("lastName").trim().concat(" ").concat(orderDetails.get("firstName").trim());
        }
        return orderDetails.get("lastName").trim().concat(" ").concat(orderDetails.get("firstName").trim()).concat(" ").concat(orderDetails.get("patronymic").trim());
    }

    public Order addProductToOrder(String productID, User user) {
        productID = productID.replaceAll("=","");

        Product product = getProduct(productID);
        Order order = getActiveOrder(user);
        order.getOrderedProducts().put(productID, 1);
        order.setTotalPrice(order.getTotalPrice() + product.getFinalPrice());
        order.setTotalBonus(order.getTotalBonus() + product.getBonus());
        orderRepo.save(order);
        return order;
    }

    public LinkedList<Object> deleteProductFromOrder(String productID, User user) {
        productID = productID.replaceAll("=","");

        Product product = getProduct(productID);
        Order order = getActiveOrder(user);
        int amount = order.getOrderedProducts().get(productID);

        order.getOrderedProducts().remove(productID);
        order.setTotalPrice(order.getTotalPrice() - product.getFinalPrice() * amount);
        order.setTotalBonus(order.getTotalBonus() - product.getBonus() * amount);
        orderRepo.save(order);
        return payloadOrderData(order);
    }
    public LinkedList<Object> increaseAmount(String productID, User user) {
        productID = productID.replaceAll("=","");

        Product product = getProduct(productID);
        Order order = getActiveOrder(user);
        int amount = order.getOrderedProducts().get(productID);

        order.getOrderedProducts().put(productID, amount + 1);
        order.setTotalPrice(order.getTotalPrice() + product.getFinalPrice());
        order.setTotalBonus(order.getTotalBonus() + product.getBonus());
        orderRepo.save(order);
        return payloadOrderData(order);
    }
    public LinkedList<Object> decreaseAmount(String productID, User user) {
        productID = productID.replaceAll("=","");

        Product product = getProduct(productID);
        Order order = getActiveOrder(user);
        int amount = order.getOrderedProducts().get(productID);

        order.getOrderedProducts().put(productID, amount - 1);
        order.setTotalPrice(order.getTotalPrice() - product.getFinalPrice());
        order.setTotalBonus(order.getTotalBonus() - product.getBonus());
        orderRepo.save(order);
        return payloadOrderData(order);
    }
    private LinkedList<Object> payloadOrderData(Order order) {
        LinkedList<Object> payload = new LinkedList<>();
        Map<String, Integer> orderedProducts = new LinkedHashMap<>();
        order.getOrderedProducts().forEach((productID, amount) -> {
            try {
                String productJson = new ObjectMapper().writeValueAsString(getProduct(productID));
                orderedProducts.put(productJson, amount);
            }
            catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        payload.add(order);
        payload.add(orderedProducts);
        return payload;
    }

    public LinkedList<Object> getOrderData(User user) {
        Order order = getActiveOrder(user);
        return payloadOrderData(order);
    }

    private Order getActiveOrder(User user) {
        if (user != null) {
            Order userOrder = orderRepo.findByUserAndAcceptedFalse(user);
            return userOrder != null ? userOrder : new Order(user);
        }
        else {
            String sessionID = RequestContextHolder.currentRequestAttributes().getSessionId();
            Order sessionOrder = orderRepo.findBySessionIDAndAcceptedFalse(sessionID);
            return sessionOrder != null ? sessionOrder : new Order(sessionID);
        }
    }

    private Product getProduct(String productID) {
        return productRepo.findByProductID(productID.replaceAll("=", ""));
    }

    public List<Order> getAllAcceptedOrders(User user) {
        List<Order> orders = orderRepo.findAllByUserAndAcceptedTrueAndCompletedFalse(user);
        orders.sort(Comparator.comparing(Order::getOpenDate).reversed());
        return orders;
    }

    public List<Order> getUserCompletedOrders(User user) {
        List<Order> orders = orderRepo.findAllByUserAndCompletedTrue(user);
        orders.sort(Comparator.comparing(Order::getOpenDate).reversed());
        return orders;

    }

    public LinkedList<Object> addSessionProductToUserOrder(Map<String, Integer> sessionProducts, User user) {
        Order order = getActiveOrder(user);

        if (sessionProducts != null) {
            sessionProducts.forEach((productID, amount) -> {
                productID = productID.replaceAll("=", "");
                Product product = productRepo.findByProductID(productID);

                order.getOrderedProducts().put(productID, amount);
                order.setTotalPrice(order.getTotalPrice() + product.getFinalPrice() * amount);
                order.setTotalBonus(order.getTotalBonus() + product.getBonus() * amount);
            });
            orderRepo.save(order);
        }

        LinkedList<Object> payload = new LinkedList<>();
        payload.add(user);
        payload.add(order);
        return payload;
    }

    public boolean confirmOrder(Long orderID) {
        Order order = orderRepo.findByOrderID(orderID);
        order.setConfirmed(true);
        orderRepo.save(order);
        /// sendNotificationToUser(order.getUser())
        return true;
    }

    public boolean completeOrder(Long orderID) {
        Order order = orderRepo.findByOrderID(orderID);
        User user = order.getUser();
        order.setCompleted(true);
        if (user != null) {
            user.setBonus(user.getBonus() + order.getTotalBonus());
            userRepo.save(user);
        }
        orderRepo.save(order);
        /// sendNotificationToUser(order.getUser())
        return true;
    }

    public boolean cancelConfirmOrder(Long orderID) {
        Order order = orderRepo.findByOrderID(orderID);
        order.setConfirmed(false);
        orderRepo.save(order);
        /// sendNotificationToUser(order.getUser())
        return true;
    }

    public List<Order> deleteOrder(Long orderID) {
        orderRepo.delete(orderRepo.findByOrderID(orderID));
        List<Order> orders = orderRepo.findAllByAcceptedTrueAndCompletedFalse();
        orders.sort(Comparator.comparing(Order::getOpenDate).reversed());
        return orders;
    }

    public List<Order> getAllAcceptedOrders() {
        List<Order> acceptedOrders = orderRepo.findAllByAcceptedTrueAndCompletedFalse();
        acceptedOrders.sort(Comparator.comparing(Order::getOpenDate).reversed());
        return acceptedOrders;
    }

    public List<Order> getAllCompletedOrders() {
        List<Order> completedOrders = orderRepo.findAllByCompletedTrue();
        completedOrders.sort(Comparator.comparing(Order::getOpenDate).reversed());
        return completedOrders;
    }

    public boolean hasCurrentSessionOrder() {
        String sessionID = RequestContextHolder.currentRequestAttributes().getSessionId();
        return orderRepo.findBySessionIDAndAcceptedFalse(sessionID) != null;
    }

    public List<Order> searchAcceptedOrders(String searchData) {
        List<Order> acceptedOrders;

        acceptedOrders = orderRepo.findByClientNameContainsIgnoreCase(searchData);
        if (!acceptedOrders.isEmpty()) return acceptedOrders;

        acceptedOrders = orderRepo.findByClientMobileContains(searchData);
        if (!acceptedOrders.isEmpty()) return acceptedOrders;

        try {
            acceptedOrders = Collections.singletonList(orderRepo.findByOrderID(Long.parseLong(searchData)));
            if (!acceptedOrders.isEmpty()) return acceptedOrders;
        }
        catch (NumberFormatException e) {
            e.getSuppressed();
        }
        return null;
    }
}
