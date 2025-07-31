package com.Huy.order_service.service;

import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.sql.Date;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.Huy.Common.Event.OrderEvent;
import com.Huy.Common.Event.ProductEvent;
import com.Huy.Common.Exception.ResourceNotFoundException;
import com.Huy.order_service.data.BankingStatus;
import com.Huy.order_service.model.CartItem;
import com.Huy.order_service.model.request;
import com.Huy.order_service.model.entity.CartModel;
import com.Huy.order_service.model.entity.Order;
import com.Huy.order_service.repository.OrderRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService {
    private final WebClient.Builder webClientBuilder;
    private final OrderRepository orderRepository;
    private final KafkaTemplate kafkaTemplate;

    public OrderService(WebClient.Builder webClientBuilder, OrderRepository orderRepository, KafkaTemplate kafkaTemplate) {
        this.webClientBuilder = webClientBuilder;
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final String Cart_Key = "CART";

    private final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";

    private String generateRandomId(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }
        return sb.toString();
    }

    public List<CartItem> getCartFromSession(HttpSession session, request rq) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute(Cart_Key + rq.getUserId());

        if (cart == null) {
            cart = new ArrayList<CartItem>();
            session.setAttribute(Cart_Key, cart);
        }
        return cart;
    }

    // Còn trường hợp race condition chưa được xử lý
    public void addToCart(CartModel cart, HttpSession session, String id) throws SQLIntegrityConstraintViolationException {
        try {
            int productDetailsId = cart.getProductDetailsId();
            int quantity = cart.getQuantity();
            int currentStock = webClientBuilder.build()
                    .get()
                    .uri("lb://product-service/api/product/product-details/{id}/quantity", productDetailsId)
                    .retrieve()
                    .bodyToMono(Integer.class)
                    .block();

            if (currentStock <= quantity) {
                throw new SQLIntegrityConstraintViolationException(
                        "Không còn đủ số lượng cho productId: " + productDetailsId);
            }

            CartItem newReservation = new CartItem(quantity, productDetailsId);
            List<CartItem> list = getCartFromSession(session, new request(id));
            var cartItemFound = list.stream()
                    .filter(item -> item.getProductDetailsId() == productDetailsId)
                    .findFirst();
            if (cartItemFound.isPresent()) {
                list.remove(cartItemFound.get());
            }
            list.add(newReservation);
            session.setAttribute(Cart_Key, list);

        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.error(e.getMessage());
                throw new ResourceNotFoundException("Không tìm thấy product");
            }
            log.error("Error during get quantity: {}", e.getMessage(), e);
            throw new RuntimeException("Error during get quantity: " + e.getMessage(), e);
        }
    }

    // Xóa đặt chỗ khi người dùng tự xóa khỏi giỏ hàng.
    public void removeFromCart(HttpSession session, int productId, request rq) {
        List<CartItem> list = getCartFromSession(session, rq);
        CartItem cartModel = list.stream().filter(p -> p.getProductDetailsId() == productId)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));
        list.remove(cartModel);
        session.setAttribute(Cart_Key, list);
    }

    @Transactional
    public Order createOrder(HttpSession session, request rq) {
        List<CartItem> list = getCartFromSession(session, rq);
        if (list == null || list.size() == 0) {
            throw new InvalidParameterException("Chưa mua hàng nào");
        }
        List<CartModel> cartModels = list.stream()
                .map(cartItem -> new CartModel(cartItem.getQuantity(), cartItem.getProductDetailsId())).toList();
        String randomId = generateRandomId(20);
        Order order = new Order(randomId, BankingStatus.PENDING.toString(), Date.valueOf(LocalDate.now()), rq.getUserId(), null);
        cartModels.forEach(cart -> cart.setOrder(order));
        order.setProducts(cartModels);
        orderRepository.save(order);
        orderRepository.flush(); // Đảm bảo order được lưu vào DB trước khi gửi sự kiện
        order.getProducts().forEach(cart -> cart.getId());
        return order;
    }

    // Xử lý khi payment thành công
    @KafkaListener(topics = "orderTopic")
    public void handleAfterPaymentSuccess(OrderEvent orderEvent) {
        String orderId = orderEvent.getId();
        Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order với id: " + orderId));

        List<com.Huy.Common.Event.CartModel> carts = order.getProducts()
                                    .stream()
                                    .map(cart -> new com.Huy.Common.Event.CartModel(cart.getProductDetailsId(), cart.getQuantity()))
                                    .toList();
        order.setStatus(BankingStatus.SUCCESS.toString());
        orderRepository.save(order);
        kafkaTemplate.send("productTopic", new ProductEvent(carts));
    }

    // Xử lý khi payment thất bại
    @KafkaListener(topics = "orderTopicFailed")
    public void handleAfterPaymentFailed(OrderEvent orderEvent) {
        String orderId = orderEvent.getId();
        Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order với id: " + orderId));
        order.setStatus(BankingStatus.FAILED.toString());
        orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders;
    }

    public Order getOrderById(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order với id: " + id));
        return order;
    }

    public List<Order> getOrdersByUserId(String userId) {
        Optional<List<Order>> optionalOrders = orderRepository.findOrderByUserId(userId);
        if (optionalOrders.isEmpty()) {
            return new ArrayList<Order>();
        }
        return optionalOrders.get();
    }
}

