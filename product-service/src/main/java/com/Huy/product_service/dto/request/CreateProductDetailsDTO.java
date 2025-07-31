package com.Huy.product_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductDetailsDTO {
    @NotBlank(message = "Color cannot be blank")
    private String color;

    @NotBlank(message = "productId cannot be blank")
    private String productId;
    @Min(0)
    private Integer quantity;

    @NotBlank(message = "Size cannot be blank")
    private String size;
}
