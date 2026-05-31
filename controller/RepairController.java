package com.dormrepair.controller;

import com.dormrepair.common.Result;
import com.dormrepair.dto.RatingDTO;
import com.dormrepair.dto.RepairHandleDTO;
import com.dormrepair.dto.RepairSubmitDTO;
import com.dormrepair.service.RepairService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repair")
public class RepairController {

    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    @PostMapping("/submit")
    public Result<?> submit(@Valid @RequestBody RepairSubmitDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return repairService.submit(dto, userId);
    }

    @GetMapping("/my")
    public Result<?> getMyRepairs(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return repairService.getMyRepairs(userId);
    }

    @GetMapping("/all")
    public Result<?> getAllRepairs() {
        return repairService.getAllRepairs();
    }

    @GetMapping("/{id}")
    public Result<?> getDetail(@PathVariable Long id) {
        return repairService.getRepairDetail(id);
    }

    @PutMapping("/handle")
    public Result<?> handle(@Valid @RequestBody RepairHandleDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return repairService.handle(dto, userId);
    }

    @PostMapping("/rate")
    public Result<?> rate(@Valid @RequestBody RatingDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return repairService.rate(dto, userId);
    }
}
