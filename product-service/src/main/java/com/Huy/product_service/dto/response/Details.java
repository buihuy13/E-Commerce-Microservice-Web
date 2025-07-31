package com.Huy.product_service.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.Huy.product_service.model.Images;
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
public class Details {
    private Integer id;
    private String color;
    private Integer quantity;
    private List<Images> images;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Date releaseDate;
    private String productId;
    private String size;

    public List<Images> getImages() {
        if (images == null) {
            images = new ArrayList<>();
        }
        return images;
    }
}
