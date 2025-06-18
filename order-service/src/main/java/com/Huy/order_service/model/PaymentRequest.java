package com.Huy.order_service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotBlank
    private String orderInfo;
    @NotNull
    private long amount;
    @NotBlank(message = "order id must not be blank")
    private String orderId;
}

