package com.dormrepair.controller;

import com.dormrepair.common.Result;
import com.dormrepair.entity.Admin;
import com.dormrepair.mapper.AdminMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormrepair.entity.Dormitory;
import com.dormrepair.mapper.DormitoryMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminMapper adminMapper;
    private final DormitoryMapper dormitoryMapper;

    public AdminController(AdminMapper adminMapper, DormitoryMapper dormitoryMapper) {
        this.adminMapper = adminMapper;
        this.dormitoryMapper = dormitoryMapper;
    }

    @GetMapping("/info")
    public Result<?> getInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUserId, userId));
        return Result.success(admin);
    }

    @GetMapping("/dormitories")
    public Result<?> listDormitories() {
        List<Dormitory> list = dormitoryMapper.selectList(null);
        List<Map<String, Object>> result = list.stream().map(d -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", d.getId());
            m.put("building", d.getBuilding());
            m.put("roomNo", d.getRoomNo());
            m.put("name", d.getBuilding() + " " + d.getRoomNo());
            return m;
        }).toList();
        return Result.success(result);
    }
}
