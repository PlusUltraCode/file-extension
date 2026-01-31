package com.flow.assign.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FixedCreateRequest {

    @NotBlank(message = "확장자를 입력해주세요")
    @Size(max = 20, message = "확장자는 최대 20자까지 입력할 수 있습니다")
    private String extension;
}
