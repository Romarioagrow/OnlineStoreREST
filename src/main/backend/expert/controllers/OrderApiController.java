package expert.controllers;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import expert.domain.Order;
import expert.domain.User;
import expert.repos.UserRepo;
import expert.services.OrderService;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Log
@RestController
@AllArgsConstructor
@RequestMapping("/api/order")
public class OrderApiController {
    private final OrderService orderService;
    private final UserRepo userRepo;

    @PostMapping("/addProduct")
    private Order addProductToOrder(@RequestBody String productID, @AuthenticationPrincipal User user) {
        return orderService.addProductToOrder(productID, user);
    }

    @PostMapping("/orderedProducts")
    private LinkedList<Object> getOrderedProducts(@AuthenticationPrincipal User user) {
        return orderService.getOrderData(user);
    }

    @PostMapping("/deleteProduct")
    private LinkedList<Object> deleteProductFromOrder(@RequestBody String productID, @AuthenticationPrincipal User user) {
        return orderService.deleteProductFromOrder(productID, user);
    }

    @PostMapping("/increaseAmount")
    private LinkedList<Object> increaseAmount(@RequestBody String productID, @AuthenticationPrincipal User user ) {
        return orderService.increaseAmount(productID, user);
    }

    @PostMapping("/decreaseAmount")
    private LinkedList<Object> decreaseAmount(@RequestBody String productID, @AuthenticationPrincipal User user ) {
        return orderService.decreaseAmount(productID, user);
    }

    @PostMapping("/acceptOrder")
    private boolean acceptOrder(@RequestBody Map<String, String> orderDetails) {
        return orderService.acceptOrder(orderDetails);
    }

    @GetMapping("/get/getAcceptedOrders")
    private List<Order> getAcceptedOrders(@AuthenticationPrincipal User user) {
        return user != null ? orderService.getAllAcceptedOrders(user) : null;
    }

    @GetMapping("/get/getCompletedOrders")
    private List<Order> getCompletedOrders(@AuthenticationPrincipal User user) {
        return user != null ? orderService.getUserCompletedOrders(user) : null;
    }

    @GetMapping("/get/showUserBonus")
    private int showUserBonus(@AuthenticationPrincipal User user) {
        return user != null ? userRepo.findByUserID(user.getUserID()).getBonus() : null;
    }

    @GetMapping("/checkSessionOrder")
    private boolean checkSessionOrder() {
        return orderService.hasCurrentSessionOrder();
    }
}
