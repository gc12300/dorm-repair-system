package com.dormrepair.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairHandleDTO {
    @NotNull(message = "报修ID不能为空")
    private Long repairId;

    @NotBlank(message = "处理状态不能为空")
    private String status;

    private String handleDesc;
}
