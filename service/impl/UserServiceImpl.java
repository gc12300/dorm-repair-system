package com.dormrepair.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormrepair.common.Result;
import com.dormrepair.dto.LoginDTO;
import com.dormrepair.dto.RegisterDTO;
import com.dormrepair.entity.Admin;
import com.dormrepair.entity.Student;
import com.dormrepair.entity.User;
import com.dormrepair.mapper.AdminMapper;
import com.dormrepair.mapper.StudentMapper;
import com.dormrepair.mapper.UserMapper;
import com.dormrepair.service.UserService;
import com.dormrepair.util.JwtUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final AdminMapper adminMapper;
    private final JwtUtil jwtUtil;

    // 去掉了 BCryptPasswordEncoder
    public UserServiceImpl(UserMapper userMapper,
                           StudentMapper studentMapper,
                           AdminMapper adminMapper,
                           JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.studentMapper = studentMapper;
        this.adminMapper = adminMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public Result<Object> register(RegisterDTO dto) {
        if (userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())) > 0) {
            return Result.error(400, "用户名已存在");
        }

        String role = dto.getRole() != null ? dto.getRole() : "STUDENT";

        User user = new User();
        user.setUsername(dto.getUsername());
        // 暂时存明文，先跑通项目
        user.setPassword(dto.getPassword());
        user.setRole(role);
        userMapper.insert(user);

        if ("ADMIN".equals(role)) {
            Admin admin = new Admin();
            admin.setUserId(user.getId());
            admin.setName(dto.getName());
            admin.setPhone(dto.getPhone());
            adminMapper.insert(admin);
        } else {
            if (dto.getDormitoryId() == null) {
                return Result.error(400, "学生必须选择宿舍楼");
            }
            Student student = new Student();
            student.setUserId(user.getId());
            student.setStudentNo(dto.getStudentNo());
            student.setName(dto.getName());
            student.setPhone(dto.getPhone());
            student.setDormitoryId(dto.getDormitoryId());
            studentMapper.insert(student);
        }

        return Result.success("注册成功");
    }

    @Override
    public Result<Object> login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));

        // 明文比对
        if (user == null || !dto.getPassword().equals(user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        return Result.success(token);
    }
}