package com.flow.assign.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "extension")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Extension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Extension of(String name, LocalDateTime now){
        return Extension.builder()
                .name(name)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

}
