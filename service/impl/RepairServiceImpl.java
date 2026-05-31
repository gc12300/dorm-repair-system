package com.dormrepair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.dormrepair.common.Result;
import com.dormrepair.dto.RatingDTO;
import com.dormrepair.dto.RepairHandleDTO;
import com.dormrepair.dto.RepairSubmitDTO;
import com.dormrepair.entity.Admin;
import com.dormrepair.entity.Repair;
import com.dormrepair.entity.Student;
import com.dormrepair.mapper.AdminMapper;
import com.dormrepair.mapper.RepairMapper;
import com.dormrepair.mapper.StudentMapper;
import com.dormrepair.service.RepairService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RepairServiceImpl implements RepairService {

    private final RepairMapper repairMapper;
    private final StudentMapper studentMapper;
    private final AdminMapper adminMapper;
    private final JdbcTemplate jdbcTemplate;

    public RepairServiceImpl(RepairMapper repairMapper, StudentMapper studentMapper,
                             AdminMapper adminMapper, JdbcTemplate jdbcTemplate) {
        this.repairMapper = repairMapper;
        this.studentMapper = studentMapper;
        this.adminMapper = adminMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Result<?> submit(RepairSubmitDTO dto, Long userId) {
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId));
        if (student == null) {
            // 补上状态码，比如400（参数错误）或404（未找到）
            return Result.error(404, "学生信息不存在");
        }

        Repair repair = new Repair();
        repair.setStudentId(student.getId());
        repair.setDormitoryId(student.getDormitoryId());
        repair.setTitle(dto.getTitle());
        repair.setDescription(dto.getDescription());
        repair.setStatus("待处理");
        repair.setCreateTime(LocalDateTime.now());
        repairMapper.insert(repair);

        return Result.success();
    }

    @Override
    public Result getMyRepairs(Long userId) {
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId));
        if (student == null) {
            return Result.error("学生信息不存在");
        }

        List<Repair> repairs = repairMapper.selectList(new LambdaQueryWrapper<Repair>()
                .eq(Repair::getStudentId, student.getId())
                .orderByDesc(Repair::getCreateTime));

        return Result.success(repairs);
    }

    @Override
    public Result<?> getAllRepairs() {
        List<Repair> repairs = repairMapper.selectList(new LambdaQueryWrapper<Repair>()
                .orderByDesc(Repair::getCreateTime));

        List<Map<String, Object>> list = repairs.stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("title", r.getTitle());
            m.put("description", r.getDescription());
            m.put("status", r.getStatus());
            m.put("handleDesc", r.getHandleDesc());
            m.put("rating", r.getRating());
            m.put("review", r.getReview());
            m.put("createTime", r.getCreateTime());

            Student s = studentMapper.selectById(r.getStudentId());
            if (s != null) {
                m.put("studentName", s.getName());
                m.put("studentNo", s.getStudentNo());
                m.put("studentPhone", s.getPhone());
            }

            if (r.getAdminId() != null) {
                Admin a = adminMapper.selectById(r.getAdminId());
                if (a != null) {
                    m.put("adminName", a.getName());
                }
            }
            return m;
        }).collect(Collectors.toList());

        return Result.success(list);
    }

    @Override
    public Result<?> getRepairDetail(Long repairId) {
        Repair repair = repairMapper.selectById(repairId);
        if (repair == null) {
            return Result.error("报修记录不存在");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", repair.getId());
        data.put("title", repair.getTitle());
        data.put("description", repair.getDescription());
        data.put("status", repair.getStatus());
        data.put("handleDesc", repair.getHandleDesc());
        data.put("rating", repair.getRating());
        data.put("review", repair.getReview());
        data.put("createTime", repair.getCreateTime());
        data.put("updateTime", repair.getUpdateTime());

        Student s = studentMapper.selectById(repair.getStudentId());
        if (s != null) {
            data.put("studentName", s.getName());
            data.put("studentNo", s.getStudentNo());
            data.put("studentPhone", s.getPhone());
        }

        if (repair.getAdminId() != null) {
            Admin a = adminMapper.selectById(repair.getAdminId());
            if (a != null) {
                data.put("adminName", a.getName());
            }
        }

        return Result.success(data);
    }

    @Override
    public Result<?> rate(RatingDTO dto, Long userId) {
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId));
        if (student == null) {
            return Result.error("学生信息不存在");
        }

        Repair repair = repairMapper.selectById(dto.getRepairId());
        if (repair == null) {
            return Result.error("报修记录不存在");
        }
        if (!repair.getStudentId().equals(student.getId())) {
            return Result.error("只能评价自己的报修");
        }
        if (!"已完成".equals(repair.getStatus()) && !"COMPLETED".equals(repair.getStatus())) {
            return Result.error("只能评价已完成的报修");
        }
        if (repair.getRating() != null) {
            return Result.error("该报修已评价过");
        }

        Repair update = new Repair();
        update.setId(dto.getRepairId());
        update.setRating(dto.getRating());
        update.setReview(dto.getReview());
        repairMapper.updateById(update);

        return Result.success("评价成功");
    }

    @Override
    public Result<?> handle(RepairHandleDTO dto, Long userId) {
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUserId, userId));
        if (admin == null) {
            return Result.error("管理员信息不存在");
        }

        if (repairMapper.selectById(dto.getRepairId()) == null) {
            return Result.error("报修记录不存在");
        }

        Repair repair = new Repair();
        repair.setId(dto.getRepairId());
        repair.setStatus(dto.getStatus());
        repair.setHandleDesc(dto.getHandleDesc());
        repair.setAdminId(admin.getId());
        repairMapper.updateById(repair);

        return Result.success("处理成功");
    }
}
