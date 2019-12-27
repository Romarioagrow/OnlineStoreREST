package expert.controllers;
import expert.dto.OrderedProduct;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import expert.domain.Product;
import expert.dto.FiltersList;
import expert.services.ProductService;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Log
@RestController
@AllArgsConstructor
@RequestMapping("/api/products")
public class ProductApiController {
    private final ProductService productService;

    @GetMapping("/build_filters/{group}")
    private FiltersList createFiltersLists(@PathVariable String group) {
        //productService.createDefaultFilterChecklist(group);
        return productService.createProductsFilterLists(group);
    }

    @GetMapping("/group/{group}/{page}")
    private Page<Product> listProductsByGroupPage(@PathVariable String group, @PathVariable(required = false) int page) {
        return productService.getProductsByGroup(group, PageRequest.of(page, 15, Sort.Direction.ASC, "pic"));
    }

    @GetMapping("/show/{productID}")
    private Product listProductByID(@PathVariable String productID) {
        return productService.getProductByID(productID);
    }

    @PostMapping("/filter/{group}/{page}")
    private LinkedList<Object>/*Page<Product>*/ filterProducts(@RequestBody LinkedList<String> filters, @PathVariable String group, @PathVariable(required = false) int page) {
        return productService.filterProducts(filters, group, PageRequest.of(page, 15, Sort.Direction.ASC, "pic"));
    }

    @PostMapping("/search")
    private List<Product> searchProducts(@RequestBody String searchRequest) {
        return productService.searchProducts(searchRequest);
    }

    @GetMapping("/getRecentProducts")
    private List<OrderedProduct> getRecentProducts() {
        return productService.getRecentProducts();
    }

    @GetMapping("/getPopularProducts")
    private List<Product> getPopularProducts() {
        return productService.getPopularProducts();
    }

    @PostMapping("/addPopularProduct")
    private List<Product> addPopularProduct(@RequestBody String productID) {
        return productService.addPopularProducts(productID);
    }

    @PostMapping("/deletePopularProduct")
    private List<Product> deletePopularProduct(@RequestBody String productID) {
        return productService.deletePopularProduct(productID);
    }

}
