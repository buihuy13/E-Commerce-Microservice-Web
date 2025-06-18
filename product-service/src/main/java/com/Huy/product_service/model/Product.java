package com.Huy.product_service.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
@Builder
public class Product {
    @Id
    private String id;
    @NotBlank(message = "Name cannot be blank")
    private String name;
    private String description;
    @NotNull(message = "Price cannot be null")
    private BigDecimal price;
    @NotBlank(message = "Category cannot be blank")
    private String category;
    @Column(name = "releaseDate")
    private Date releaseDate;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductDetails> productDetailsList;

    public List<ProductDetails> getProductDetailsList() {
        if (productDetailsList == null)
        {
            productDetailsList = new ArrayList<>();
        }
        return productDetailsList;
    }
}
