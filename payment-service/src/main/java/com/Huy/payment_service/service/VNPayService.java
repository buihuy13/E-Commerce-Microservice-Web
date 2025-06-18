package com.Huy.payment_service.service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.Huy.Common.Event.NotificationEvent;
import com.Huy.Common.Event.OrderEvent;
import com.Huy.payment_service.config.VNPayConfig;
import com.Huy.payment_service.model.PaymentRequest;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class VNPayService {
    private final KafkaTemplate kafkaTemplate;

    public VNPayService(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    @Value("${vnpay.tmn_code}")
    private String vnpay_tmncode;

    @Value("${vnpay.hash_secret}")
    private String vnpay_hashsecret;

    @Value("${vnpay.url}")
    private String vnpay_payurl;

    @Value("${vnpay.return_url}")
    private String vnpay_returnurl;

    @Value("${vnpay.ipn_url}")
    private String vnapy_ipnurl;

    @Value("${frontend.success}")
    private String success_frontend;

    @Value("${frontend.failed}")
    private String failed_frontend;

    public String createOrder(HttpServletRequest httpRequest, PaymentRequest payment)
            throws UnsupportedEncodingException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = payment.getOrderId();
        String vnp_IpAddr = VNPayConfig.getIpAddress(httpRequest);
        String vnp_TmnCode = vnpay_tmncode;
        String orderType = "order-type";
        String orderInfor = payment.getOrderInfo();
        long total = payment.getAmount();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(total * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfor);
        vnp_Params.put("vnp_OrderType", orderType);

        String locate = "vn";
        vnp_Params.put("vnp_Locale", locate);

        vnp_Params.put("vnp_ReturnUrl", vnpay_returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = (String) fieldNames.get(i);
            String fieldValue = (String) vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                // Build hash data
                // Sửa thành UTF-8
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                // Thêm dấu & một cách an toàn hơn
                if (i < fieldNames.size() - 1) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnpay_hashsecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnpay_payurl + "?" + queryUrl;
        return paymentUrl;
    }

    public void handlePayment(HttpServletRequest request) {
        //Lấy tất cả tham số và sắp xếp
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        //Xóa các key này ra khỏi map để tạo lại hashData
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        //Tạo lại chuỗi hashData và xác thực chữ ký
        String signValue = VNPayConfig.hashAllFields(fields); // hashAllFields phải sắp xếp key theo ABC

        if (!signValue.equals(vnp_SecureHash)) {
            // Chữ ký không hợp lệ -> Throw ra lỗi
        }

        String email = request.getParameter("vnp_OrderInfo");
        String orderId = request.getParameter("vnp_TxnRef");

        //Thanh toán thành công
        if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
            kafkaTemplate.send("orderTopic", new OrderEvent(orderId));
            kafkaTemplate.send("notificationTopic", new NotificationEvent(email, true));
        } else {
            // Giao dịch thất bại
            // Xử lý đối với giao dịch fail
            kafkaTemplate.send("NotificationTopic", new NotificationEvent(email, false));
        }
    }

    public String paymentUrlReturn(HttpServletRequest request) {
        String responseCode = request.getParameter("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            return success_frontend;
        }
        return failed_frontend;
    }
}
