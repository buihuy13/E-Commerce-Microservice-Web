package com.Huy.product_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Huy.product_service.model.Images;

@Repository
public interface ImagesRepository extends JpaRepository<Images, Long> {

}
