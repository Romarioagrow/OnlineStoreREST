package expert.controllers;

import expert.domain.Order;
import expert.domain.Product;
import expert.domain.User;
import expert.services.OrderService;
import expert.services.ProductBuilder;
import expert.services.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log
@RestController
@AllArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminApiController {
    private final ProductBuilder productBuilder;
    private final OrderService orderService;
    private final ProductService productService;


    @PostMapping("/updateDB")
    private void updateDB(
            @RequestParam("file[0]") MultipartFile file1,
            @RequestParam("file[1]") MultipartFile file2
    ){
        ArrayList<MultipartFile> supplierCatalogs = new ArrayList<>();
        supplierCatalogs.add(file1);
        supplierCatalogs.add(file2);
        productBuilder.updateProductsDB(supplierCatalogs);
    }


    @PostMapping("/restoreDefaultPrice")
    private Product restoreDefaultPrice(@RequestBody String productID) {
        log.info(productID);
        return productBuilder.restoreDefaultPrice(productID);
    }

    @PostMapping("/setCustomPrice")
    private Product setCustomPrice(@RequestBody Map<String, String> priceUpdate) {
        return productBuilder.setCustomPrice(priceUpdate);
    }

    @PostMapping("/searchAcceptedOrders")
    private List<Order> searchAcceptedOrders(@RequestBody String searchData) {
        return orderService.searchAcceptedOrders(searchData);
    }

    @PostMapping("/deleteOrder")
    private List<Order> deleteOrder(@RequestBody String orderID) {
        return orderService.deleteOrder(Long.parseLong(orderID));
    }

    @PostMapping("/completeOrder")
    private boolean completeOrder(@RequestBody String orderID) {
        return orderService.completeOrder(Long.parseLong(orderID));
    }

    @PostMapping("/confirmOrder")
    private boolean acceptOrder(@RequestBody String orderID) {
        return orderService.confirmOrder(Long.parseLong(orderID));
    }

    @PostMapping("/cancelConfirmOrder")
    private boolean cancelConfirmOrder(@RequestBody String orderID) {
        return orderService.cancelConfirmOrder(Long.parseLong(orderID));
    }

    @GetMapping("/acceptedOrders")
    private List<Order> getAllAcceptedOrders() {
        return orderService.getAllAcceptedOrders();
    }

    @GetMapping("/completedOrders")
    private List<Order> getAllCompletedOrders() {
        return orderService.getAllCompletedOrders();
    }

    @GetMapping
    private boolean isAdmin(@AuthenticationPrincipal User user) {
        if (user == null) return false;
        return user.isAdmin();
    }

    @PostMapping("/updateCatalog")
    private void updateProductsCatalog() {
        try
        {
            productBuilder.mapCatalogJSON();
        }
        catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/uploadFileBrands")
    private void uploadBrandsPrice(@RequestParam("fileBrands") MultipartFile file) {
        productBuilder.updateBrandsPrice(file);
    }

    @PostMapping("/parsePicsRUSBT")
    private void parsePicsRUSBT() {
        productBuilder.parsePicsRUSBT();
    }

    @PostMapping("/downloadImage")
    private boolean downloadImage(@RequestBody Map<String, String> data) {
        return productBuilder.downloadImage(data);
    }

    @PostMapping("/resetDB")
    private void resetDB() {
        productBuilder.resetDB();
    }

    @PostMapping("/test")
    private void test(@AuthenticationPrincipal User user) {
        productBuilder.test();
    }
}


