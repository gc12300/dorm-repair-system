package com.dormrepair.service;

import com.dormrepair.common.Result;
import com.dormrepair.dto.RatingDTO;
import com.dormrepair.dto.RepairHandleDTO;
import com.dormrepair.dto.RepairSubmitDTO;

public interface RepairService {
    Result<?> submit(RepairSubmitDTO dto, Long userId);
    Result<?> getMyRepairs(Long userId);
    Result<?> getAllRepairs();
    Result<?> getRepairDetail(Long repairId);
    Result<?> handle(RepairHandleDTO dto, Long userId);
    Result<?> rate(RatingDTO dto, Long userId);
}
