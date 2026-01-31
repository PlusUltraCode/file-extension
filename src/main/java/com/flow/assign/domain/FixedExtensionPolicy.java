package com.flow.assign.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "fixed_extension_policy")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedExtensionPolicy {

    @Id
    @Column(length = 20, nullable = false)
    private String extension;

    @Column(nullable = false)
    private boolean blocked;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static FixedExtensionPolicy of(String extension, LocalDateTime now){
        return FixedExtensionPolicy.builder()
                .extension(extension)
                .createdAt(now)
                .updatedAt(now)
                .blocked(true)
                .build();
    }

    public void updateBlocked(boolean blocked, LocalDateTime now) {
        this.blocked = blocked;
        this.updatedAt = now;
    }
}
