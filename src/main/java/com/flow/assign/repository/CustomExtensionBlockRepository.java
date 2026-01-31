package com.flow.assign.repository;

import com.flow.assign.domain.CustomExtensionBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface CustomExtensionBlockRepository extends JpaRepository<CustomExtensionBlock, Long> {

    Optional<CustomExtensionBlock> findByExtension(String extension);

    boolean existsByExtension(String extension);

    void deleteByExtension(String extension);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from CustomExtensionBlock b where b.extension = :extension")
    Optional<CustomExtensionBlock> findByExtensionForUpdate(@Param("extension") String extension);

    long countByExtensionNot(String extension);

    List<CustomExtensionBlock> findAllByExtensionNot(String extension);

    Page<CustomExtensionBlock> findAllByExtensionNot(String extension, Pageable pageable);
}
