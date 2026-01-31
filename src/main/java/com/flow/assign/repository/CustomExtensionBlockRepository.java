package com.flow.assign.repository;

import com.flow.assign.domain.CustomExtensionBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomExtensionBlockRepository extends JpaRepository<CustomExtensionBlock, Long> {
    Optional<CustomExtensionBlock> findByExtension(String extension);
    boolean existsByExtension(String extension);
    long countBy();
}
