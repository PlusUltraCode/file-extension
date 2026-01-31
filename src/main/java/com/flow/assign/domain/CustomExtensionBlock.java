package com.flow.assign.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "custom_extension_block")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomExtensionBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false, unique = true)
    private String extension;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static CustomExtensionBlock of (String extension, LocalDateTime now){
        return CustomExtensionBlock.builder()
                .extension(extension)
                .createdAt(now)
                .build();
    }

    public void changeExtension(String extension) {
        this.extension = extension;
    }

}
