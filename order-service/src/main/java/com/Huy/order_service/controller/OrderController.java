package com.Huy.order_service.controller;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.Huy.order_service.model.MessageResponse;
import com.Huy.order_service.model.request;
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

    @PostMapping("/{id}")
    @CircuitBreaker(name = "order_product", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "order_product")
    @Retry(name = "order_product")
    public CompletableFuture<ResponseEntity<MessageResponse>> addToCart(@RequestBody @Valid CartItem cart,
                                                                        HttpSession session, 
                                                                        @PathVariable String id) throws SQLIntegrityConstraintViolationException
    {
        return CompletableFuture.supplyAsync(() -> {
            try {
                orderService.addToCart(cart, session, id);
                return ResponseEntity.ok(new MessageResponse("Thêm thành công"));
            }
            catch(ResourceNotFoundException ex) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
            }
            catch(SQLIntegrityConstraintViolationException ex) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
            }
        });
    }

    public CompletableFuture<String> fallbackMethod(CartItem cart, RuntimeException ex)
    {
        return CompletableFuture.supplyAsync(() -> "Oops! Something went wrong, please wait for 5 more minutes~");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteToCart(@PathVariable int id, HttpSession session,@RequestBody request rq)
    {
        orderService.removeFromCart(session, id, rq);
        return ResponseEntity.ok(new MessageResponse("Xóa thành công"));
    }

    @PostMapping("/payment")
    public ResponseEntity<Order> buyProducts(HttpSession session, request rq)
    {
        Order res = orderService.createOrder(session,rq);
        return ResponseEntity.ok(res);
    }

    @GetMapping() 
    public ResponseEntity<List<CartItem>> getAllItems(HttpSession session,@RequestBody request rq)
    {
        List<CartItem> cart = orderService.getCartFromSession(session, rq);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Order>> getAllOrders()
    {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id)
    {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable String userId)
    {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }
}
