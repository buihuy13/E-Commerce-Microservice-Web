package com.Huy.payment_service.config;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class VNPayConfig {

    // Phương thức mã hóa HmacSHA512
    // Tạo chữ ký số -> toàn vẹn dữ liệu và đảm bảo nguồn gốc
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            // convert khóa thành mảng byte theo chuẩn dạng UTF-8
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            // convert dữ liệu cần mã hóa thành mảng byte theo chuẩn dạng UTF-8
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            // thực hiện mã hóa dữ liệu -> mảng byte kết quả sau khi mã hóa
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

    // Util for VNPAY
    // Phương thức mã hóa tất cả các trường dữ liệu theo định dạng "key1=value1&key2=value2..."
    // -> tạo chữ ký số cho các trường dữ liệu (vnp_SecureHash)
    public static String hashAllFields(Map<String, String> fields, String vnpay_hashsecret) {
        String hashData = fields.entrySet().stream()
                // Lọc các trường không null và không rỗng
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey()) // Sắp xếp các trường theo thứ tự ABC của tên (key)
                .map(entry -> entry.getKey() + "=" + entry.getValue()) // Chuyển đổi thành chuỗi "key=value"
                .collect(Collectors.joining("&")); // Nối chúng lại thành chuỗi dạng "key1=value1&key2=value2..."

        // 4. Mã hóa chuỗi đã tạo bằng HmacSHA512
        return hmacSHA512(vnpay_hashsecret, hashData);
    }
}