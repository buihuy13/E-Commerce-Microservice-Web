package com.Huy.order_service.controller;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.Huy.order_service.model.MessageResponse;
import com.Huy.order_service.model.entity.CartModel;
import com.Huy.order_service.model.entity.Order;
import com.Huy.order_service.service.OrderService;
import com.Huy.order_service.model.CartItem;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping()
    @CircuitBreaker(name = "order_product", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "order_product")
    @Retry(name = "order_product")
    public CompletableFuture<ResponseEntity<MessageResponse>> addToCart(@RequestBody @Valid CartModel cart, HttpSession session) throws SQLIntegrityConstraintViolationException
    {
        orderService.addToCart(cart, session);
        return CompletableFuture.supplyAsync(() ->ResponseEntity.ok(new MessageResponse("Thêm thành công")));
    }

    public CompletableFuture<String> fallbackMethod(CartModel cart, RuntimeException ex)
    {
        return CompletableFuture.supplyAsync(() -> "Oops! Something went wrong, please wait for 5 more minutes~");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteToCart(@PathVariable int id, HttpSession session)
    {
        orderService.removeFromCart(session, id);
        return ResponseEntity.ok(new MessageResponse("Xóa thành công"));
    }

    @PostMapping("/payment")
    public ResponseEntity<Order> buyProducts(HttpSession session)
    {
        Order res = orderService.createOrder(session);
        return ResponseEntity.ok(res);
    }

    @GetMapping() 
    public ResponseEntity<List<CartItem>> getAllItems(HttpSession session)
    {
        List<CartItem> cart = orderService.getCartFromSession(session);
        return ResponseEntity.ok(cart);
    }
}
