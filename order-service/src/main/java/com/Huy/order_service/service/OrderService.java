package com.Huy.order_service.service;

import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
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
import com.Huy.order_service.model.CartModel;
import com.Huy.order_service.model.Order;
import com.Huy.order_service.model.OrderRequest;
import com.Huy.order_service.model.PaymentRequest;
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

    private List<CartItem> getCartFromSession(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute(Cart_Key);

        if (cart == null) {
            cart = new ArrayList<CartItem>();
            session.setAttribute(Cart_Key, cart);
        }
        return cart;
    }

    public void addToCart(CartModel cart, HttpSession session)
            throws SQLIntegrityConstraintViolationException {
        try {
            int productDetailsId = cart.getProductDetailsId();
            int quantity = cart.getQuantity();
            int currentStock = webClientBuilder.build()
                    .get()
                    .uri("lb://product-service/api/product-details/{id}/quantity", productDetailsId)
                    .retrieve()
                    .bodyToMono(Integer.class)
                    .block();

            if (currentStock > quantity) {
                CartItem newReservation = new CartItem(quantity, productDetailsId);

                List<CartItem> list = getCartFromSession(session);
                list.add(newReservation);
                session.setAttribute(Cart_Key, list);

            } else {
                throw new SQLIntegrityConstraintViolationException(
                        "Không còn đủ số lượng cho productId: " + productDetailsId);
            }
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {

                log.error(e.getMessage());
                throw new ResourceNotFoundException("Không tìm thấy product");
            }
            log.error("Error during get quantity: {}", e.getMessage(), e);
            throw new RuntimeException("Error during get quantity: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa đặt chỗ khi người dùng tự xóa khỏi giỏ hàng.
     */
    public void removeFromCart(HttpSession session, Integer productId) {
        List<CartItem> list = getCartFromSession(session);
        CartItem cartModel = list.stream().filter(p -> p.getProductDetailsId() == productId)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm trong giỏ hàng"));
        list.remove(cartModel);
        session.setAttribute(Cart_Key, list);
    }

    @Transactional
    public String buyProducts(HttpSession session, OrderRequest orderRequest) {
        List<CartItem> list = getCartFromSession(session);
        if (list == null || list.size() == 0) {
            throw new InvalidParameterException("Chưa mua hàng nào");
        }
        List<CartModel> cartModels = list.stream()
                .map(cartItem -> new CartModel(cartItem.getQuantity(), cartItem.getProductDetailsId())).toList();
        String randomId = generateRandomId(20);
        Order order = new Order(randomId, BankingStatus.PENDING.toString(), null);
        cartModels.forEach(cart -> cart.setOrder(order));
        order.setProducts(cartModels);
        orderRepository.save(order);
        orderRepository.flush();

        String url = webClientBuilder.build()
                .post()
                .uri("lb://payment-service/api/payment")
                .bodyValue(new PaymentRequest(orderRequest.getOrderInfor(), orderRequest.getAmount(), randomId))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        session.removeAttribute(Cart_Key);
        // gọi đến payment url để banking
        return url;
    }

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

    @KafkaListener(topics = "orderTopicFailed")
    public void handleAfterPaymentFailed(OrderEvent orderEvent) {
        String orderId = orderEvent.getId();
        Order order = orderRepository.findById(orderId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order với id: " + orderId));
        order.setStatus(BankingStatus.FAILED.toString());
        orderRepository.save(order);
    }
}

