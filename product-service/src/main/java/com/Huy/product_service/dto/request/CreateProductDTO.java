package com.Huy.product_service.dto.request;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductDTO {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    private String description;
    @NotNull(message = "Price cannot be null")
    private BigDecimal price;
    @NotBlank(message = "Category cannot be blank")
    private String category;
    @Column(name = "releaseDate")
    private Date releaseDate;
}
