package com.Huy.product_service.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.Huy.product_service.dto.request.CreateProductDetailsDTO;
import com.Huy.product_service.dto.response.MessageResponse;
import com.Huy.product_service.dto.response.Quantity;
import com.Huy.product_service.model.ProductDetails;
import com.Huy.product_service.service.ProductDetailsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/product/product-details")
public class ProductDetailsController {
    private final ProductDetailsService productDetailsService;
    public ProductDetailsController(ProductDetailsService productDetailsService) {
        this.productDetailsService = productDetailsService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetails> getProductById(@PathVariable int id)
    {
        ProductDetails product = productDetailsService.getProductDetailsById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping()
    public ResponseEntity<List<ProductDetails>> getProducts()
    {
        return ResponseEntity.ok(productDetailsService.getProductDetails());
    }

    @PostMapping()
    public ResponseEntity<ProductDetails> createProduct(@RequestPart(value = "product", required = true) @Valid CreateProductDetailsDTO productDTO,
                                                        @RequestPart(value = "image", required = false) List<MultipartFile> imageFile) throws IOException
    {
        ProductDetails product = productDetailsService.createProductDetails(productDTO, imageFile);
        return new ResponseEntity<>(product, HttpStatusCode.valueOf(201));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDetails> updateProduct(@PathVariable int id, 
                    @RequestPart(value = "product", required = true) @Valid CreateProductDetailsDTO productDTO, 
                    @RequestPart(value = "image", required = false) List<MultipartFile> imageFile) throws IOException
    {
        ProductDetails product = productDetailsService.updateProductDetails(id, productDTO, imageFile);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable int id)
    {
        productDetailsService.deleteProductDetails(id);
        return ResponseEntity.ok(new MessageResponse("Xóa thành công"));
    }

    @GetMapping("/{id}/quantity")
    public ResponseEntity<Quantity> getQuantity(@PathVariable int id)
    {
        ProductDetails productDetails = productDetailsService.getProductDetailsById(id);
        return ResponseEntity.ok(new Quantity(productDetails.getQuantity()));
    } 
}
