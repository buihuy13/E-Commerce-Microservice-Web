package com.Huy.product_service.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Huy.Common.Exception.ResourceNotFoundException;
import com.Huy.product_service.data.ProductCategories;
import com.Huy.product_service.dto.request.CreateProductDTO;
import com.Huy.product_service.model.Product;
import com.Huy.product_service.repository.ProductRepository;
import com.Huy.product_service.utils.Utils;

import jakarta.transaction.Transactional;


@Service
public class ProductService {
    private final ProductRepository productRepository;
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product getProductById(String id)
    {
        Product product = productRepository.findById(id)
                                            .orElseThrow(() -> new ResourceNotFoundException("Cannot find product with id: " + id));
        
        return product;
    }

    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    @Transactional
    public Product createProduct(CreateProductDTO productDTO)
    {
        Product product = Utils.mapCreateProductDTOtoProduct(productDTO);
        productRepository.save(product);
        return product;
    }

    @Transactional
    public Product updateProduct(String id, CreateProductDTO productDTO)
    {
        Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Cannot find product with id: " + id));
        
        product.setCategory(productDTO.getCategory());
        product.setDescription(productDTO.getDescription());
        product.setName(productDTO.getName());
        product.setPrice(productDTO.getPrice());
        product.setReleaseDate(productDTO.getReleaseDate());
        return product;
    }

    public void deleteProduct(String id)
    {
        Product product = productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Cannot find product with id: " + id));
        
        productRepository.delete(product);
    }

    public List<String> getCategories()
    {
        List<String> categories = new ArrayList<>();
        for (var cate : ProductCategories.values()) {
            categories.add(cate.toString());
        }
        return categories;
    }
}
