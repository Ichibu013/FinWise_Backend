package com.fintech.backend.repository;

import com.fintech.backend.models.Chian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChainRepository extends JpaRepository<Chian, Long> {
    Chian findByChainName(String chainName);
}
