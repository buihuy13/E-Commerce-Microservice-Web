package com.Huy.product_service.utils;

import java.security.SecureRandom;

import com.Huy.product_service.dto.request.CreateProductDTO;
import com.Huy.product_service.model.Product;

public class Utils {

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz";

    private static String generateRandomId(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }
        return sb.toString();
    }

    public static Product mapCreateProductDTOtoProduct(CreateProductDTO productDTO)
    {
        return Product.builder().id(generateRandomId(10))
                                .description(productDTO.getDescription())
                                .category(productDTO.getCategory())
                                .name(productDTO.getName())
                                .price(productDTO.getPrice())
                                .releaseDate(productDTO.getReleaseDate())
                                .build();                
    }
}
