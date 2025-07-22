package com.Huy.product_service.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.Huy.Common.Event.CartModel;
import com.Huy.Common.Event.ProductEvent;
import com.Huy.Common.Exception.ResourceNotFoundException;
import com.Huy.product_service.dto.request.CreateProductDetailsDTO;
import com.Huy.product_service.dto.response.Details;
import com.Huy.product_service.model.Images;
import com.Huy.product_service.model.Product;
import com.Huy.product_service.model.ProductDetails;
import com.Huy.product_service.repository.ProductDetailsRepository;
import com.Huy.product_service.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductDetailsService {
    private final ProductDetailsRepository productDetailsRepository;
    private final ProductRepository productRepository;
    public ProductDetailsService(ProductDetailsRepository productDetailsRepository, 
                                 ProductRepository productRepository) {
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
    public ProductDetails createProductDetails(CreateProductDetailsDTO productDetailsDTO, List<MultipartFile> imageFile) throws IOException {
        Product product = productRepository.findById(productDetailsDTO.getProductId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Cannot find product with id: " + productDetailsDTO.getProductId()));

        ProductDetails productDetails = ProductDetails.builder()
                                                    .color(productDetailsDTO.getColor())
                                                    .product(product)
                                                    .quantity(productDetailsDTO.getQuantity())
                                                    .build();

        if (imageFile != null && imageFile.size() > 0)
        {
            List<Images> images = new ArrayList<>();
            for (MultipartFile file : imageFile) {
                images.add(Images.builder()
                                .imageName(file.getOriginalFilename())
                                .imageType(file.getContentType())
                                .imageData(file.getBytes())
                                .productDetails(productDetails)
                                .build());
            }
            productDetails.setImages(images);
        }
        productDetailsRepository.save(productDetails);

        return productDetails;
    }

    @Transactional
    public ProductDetails updateProductDetails(int id, CreateProductDetailsDTO productDetailsDTO, List<MultipartFile> imageFile) throws IOException {
        ProductDetails productDetails = productDetailsRepository.findById(id)
                                            .orElseThrow(() -> new ResourceNotFoundException("Cannot find productdetails with id: " + id));

        Product product = productRepository.findById(productDetailsDTO.getProductId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Cannot find product with id: " + productDetailsDTO.getProductId()));
        productDetails.setColor(productDetailsDTO.getColor());
        productDetails.setQuantity(productDetailsDTO.getQuantity());
        productDetails.setProduct(product);
        
        if (imageFile != null && imageFile.size() > 0)
        {
            List<Images> images = new ArrayList<>();
            for (MultipartFile file : imageFile) {
                images.add(Images.builder()
                                .imageName(file.getOriginalFilename())
                                .imageType(file.getContentType())
                                .imageData(file.getBytes())
                                .productDetails(productDetails)
                                .build());
            }
            productDetails.setImages(images);
        }

        productDetailsRepository.save(productDetails);

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

    public Details getProductDetailsInformation(int id) {
        ProductDetails productDetails = productDetailsRepository.findById(id)
                                            .orElseThrow(() -> new ResourceNotFoundException("Cannot find productdetails with id: " + id));

        
        String productId = productDetails.ProductInformationClone().getId();
        Product product = productRepository.findById(productId)
                                            .orElseThrow(() -> new ResourceNotFoundException("Cannot find product with id: " + productId));

        return Details.builder()
                        .id(productDetails.getId())
                        .productId(productId)
                        .color(productDetails.getColor())
                        .quantity(productDetails.getQuantity())
                        .images(productDetails.getImages())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .category(product.getCategory())
                        .releaseDate(product.getReleaseDate())
                        .build();                               
    }
}
