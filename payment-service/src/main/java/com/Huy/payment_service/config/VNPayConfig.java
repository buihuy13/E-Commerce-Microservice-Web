package com.Huy.payment_service.config;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class VNPayConfig {
    @Value("${vnpay.tmn_code}")
    private String vnpay_tmncode;

    @Value("${vnpay.hash_secret}")
    private static String vnpay_hashsecret;

    @Value("${vnpay.url}")
    private String vnpay_payurl;

    @Value("${vnpay.return_url}")
    private String vnpay_returnurl;

    @Value("${vnpay.ipn_url}")
    private String vnapy_ipnurl;

     // Phương thức mã hóa HmacSHA512
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception e) {
            return "";
        }
    }

    // Phương thức lấy địa chỉ IP của client
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    //Util for VNPAY
    public static String hashAllFields(Map<String, String> fields) {
        // 1. Lọc ra các trường có giá trị và không rỗng
        // 2. Sắp xếp các trường theo thứ tự ABC của tên (key)
        // 3. Nối chúng lại thành chuỗi dạng "key1=value1&key2=value2..."
        String hashData = fields.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        // 4. Mã hóa chuỗi đã tạo bằng HmacSHA512
        return hmacSHA512(vnpay_hashsecret, hashData);
    }
}