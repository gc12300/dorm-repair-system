package com.dormrepair.controller;

import com.dormrepair.common.Result;
import com.dormrepair.dto.LoginDTO;
import com.dormrepair.dto.RegisterDTO;
import com.dormrepair.entity.User;
import com.dormrepair.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return userService.login(dto);
    }
}
