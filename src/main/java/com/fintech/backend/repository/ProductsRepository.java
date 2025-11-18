package com.fintech.backend.repository;

import com.fintech.backend.models.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductsRepository extends JpaRepository<Products, String> {
    Products findByProductName(String productName);
}
