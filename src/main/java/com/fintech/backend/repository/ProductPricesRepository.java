package com.fintech.backend.repository;

import com.fintech.backend.models.ProductPrices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPricesRepository extends JpaRepository<ProductPrices, Long> {
}
