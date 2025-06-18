package com.Huy.payment_service.controller;

import java.io.UnsupportedEncodingException;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.Huy.payment_service.model.PaymentRequest;
import com.Huy.payment_service.model.PaymentResponse;
import com.Huy.payment_service.service.VNPayService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/api/payment")
public class VNPayController {

    private VNPayService vnPayService;
    public VNPayController(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @PostMapping()
    public ResponseEntity<PaymentResponse> createPayment(HttpServletRequest request, @Valid @RequestBody PaymentRequest paymentRequest) throws UnsupportedEncodingException
    {
        String url = vnPayService.createOrder(request, paymentRequest);
        return new ResponseEntity(new PaymentResponse(url), HttpStatusCode.valueOf(201));
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<PaymentResponse> returnURL(HttpServletRequest request)
    {
        String result = vnPayService.paymentUrlReturn(request);
        return ResponseEntity.ok(new PaymentResponse(result));
    }
    
    @GetMapping("/vnpay-ipn")
    public ResponseEntity<PaymentResponse> ipnURL(HttpServletRequest request)
    {
        vnPayService.handlePayment(request);
        return ResponseEntity.ok(new PaymentResponse("Thành công xử lý đơn hàng"));
    }
}
