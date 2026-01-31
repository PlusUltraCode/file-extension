package com.flow.assign.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FixedUpdateRequest {

    @NotNull(message = "blocked 값은 필수입니다")
    private Boolean blocked;
}
