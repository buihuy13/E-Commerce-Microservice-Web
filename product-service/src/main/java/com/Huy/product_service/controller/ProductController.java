package com.Huy.product_service.controller;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Huy.product_service.dto.request.CreateProductDTO;
import com.Huy.product_service.dto.response.Data;
import com.Huy.product_service.dto.response.MessageResponse;
import com.Huy.product_service.model.Product;
import com.Huy.product_service.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id)
    {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping()
    public ResponseEntity<List<Product>> getProducts()
    {
        return ResponseEntity.ok(productService.getProducts());
    }

    @PostMapping()
    public ResponseEntity<Product> createProduct(@RequestBody @Valid CreateProductDTO productDTO)
    {
        Product product = productService.createProduct(productDTO);
        return new ResponseEntity<>(product, HttpStatusCode.valueOf(201));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody @Valid CreateProductDTO productDTO)
    {
        Product product = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteProduct(@PathVariable String id)
    {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new MessageResponse("Xóa thành công"));
    }

    @GetMapping("/categories")
    public ResponseEntity<Data> getCategories() {
        return ResponseEntity.ok(new Data(productService.getCategories()));
    }
}
