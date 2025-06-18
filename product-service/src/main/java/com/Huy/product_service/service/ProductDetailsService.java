package com.Huy.product_service.service;

import java.io.IOException;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.Huy.Common.Event.CartModel;
import com.Huy.Common.Event.ProductEvent;
import com.Huy.Common.Exception.ResourceNotFoundException;
import com.Huy.product_service.dto.request.CreateProductDetailsDTO;
import com.Huy.product_service.model.Product;
import com.Huy.product_service.model.ProductDetails;
import com.Huy.product_service.repository.ProductDetailsRepository;
import com.Huy.product_service.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductDetailsService {
    private final ProductDetailsRepository productDetailsRepository;
    private final ProductRepository productRepository;
    public ProductDetailsService(ProductDetailsRepository productDetailsRepository, ProductRepository productRepository) {
        this.productDetailsRepository = productDetailsRepository;
        this.productRepository = productRepository;
    }

    public ProductDetails getProductDetailsById(int id)
    {
        ProductDetails productDetails = productDetailsRepository.findById(id)
                                            .orElseThrow(() -> new ResourceNotFoundException("Cannot find productdetails with id: " + id));
                                        
        return productDetails;
    }

    public List<ProductDetails> getProductDetails()
    {
        return productDetailsRepository.findAll();
    }

    @Transactional
    public ProductDetails createProductDetails(CreateProductDetailsDTO productDetailsDTO, MultipartFile imageFile) throws IOException {
        Product product = productRepository.findById(productDetailsDTO.getProductId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Cannot find product with id: " + productDetailsDTO.getProductId()));

        ProductDetails productDetails = ProductDetails.builder()
                                                    .color(productDetailsDTO.getColor())
                                                    .product(product)
                                                    .quantity(productDetailsDTO.getQuantity())
                                                    .imageName(imageFile.getOriginalFilename())
                                                    .imageType(imageFile.getContentType())
                                                    .imageData(imageFile.getBytes())
                                                    .build();

        return productDetails;

    }

    @Transactional
    public ProductDetails updateProductDetails(int id, CreateProductDetailsDTO productDetailsDTO, MultipartFile imageFile) throws IOException {
        ProductDetails productDetails = productDetailsRepository.findById(id)
                                            .orElseThrow(() -> new ResourceNotFoundException("Cannot find productdetails with id: " + id));

        Product product = productRepository.findById(productDetailsDTO.getProductId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Cannot find product with id: " + productDetailsDTO.getProductId()));
        productDetails.setColor(productDetailsDTO.getColor());
        productDetails.setQuantity(productDetailsDTO.getQuantity());
        productDetails.setProduct(product);
        
        if (imageFile != null)
        {
            productDetails.setImageName(imageFile.getOriginalFilename());
            productDetails.setImageType(imageFile.getContentType());
            productDetails.setImageData(imageFile.getBytes());
        }

        return productDetails;
    }

    public void deleteProductDetails(int id)
    {
        ProductDetails productDetails = productDetailsRepository.findById(id)
                                            .orElseThrow(() -> new ResourceNotFoundException("Cannot find productdetails with id: " + id));
                                    
        productDetailsRepository.delete(productDetails);
    }

    @KafkaListener(topics = "productTopic")
    public void handleProductEvent(ProductEvent productEvent) {
        List<CartModel> products = productEvent.getCarts();

        if (products != null && products.size() > 0) {
            for (CartModel cart : products) {
                ProductDetails pd = productDetailsRepository.findById(cart.getProductDetailsId())
                                            .orElseThrow(() -> new ResourceNotFoundException("Cannot find productdetails with id: " + cart.getProductDetailsId()));
                
                pd.setQuantity(pd.getQuantity() - cart.getQuantity());
                productDetailsRepository.save(pd);
            }
        }
    }
}
