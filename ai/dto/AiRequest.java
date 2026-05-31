package com.dormrepair.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiRequest {
    @NotBlank(message = "查询内容不能为空")
    private String query;

    private Long repairId;
}
