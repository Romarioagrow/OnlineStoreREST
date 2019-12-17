package expert.controllers;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import expert.domain.User;
import expert.services.OrderService;
import expert.services.UserService;
import java.util.LinkedList;
import java.util.Map;

@Log
@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthApiController {
    private final UserService userService;
    private final OrderService orderService;

    @PostMapping("/addSessionProductToUserOrder")
    private LinkedList<Object> addSessionProductToUserOrder(@RequestBody(required = false) Map<String, Integer> sessionProducts, @AuthenticationPrincipal User user) {
        return orderService.addSessionProductToUserOrder(sessionProducts, user);
    }

    @PostMapping("/user/registration")
    private boolean registration(@RequestBody Map<String, String> userCredentials) {
        return userService.registerUser(userCredentials);
    }

    @PostMapping("/noUser")
    private boolean noUser(@AuthenticationPrincipal User user) {
        return user == null;
    }

    @PostMapping("/hasUser")
    private boolean hasUser(@AuthenticationPrincipal User user) {
        return user != null;
    }
}
