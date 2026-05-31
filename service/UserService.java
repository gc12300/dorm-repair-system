package com.dormrepair.service;

import com.dormrepair.common.Result;
import com.dormrepair.dto.LoginDTO;
import com.dormrepair.dto.RegisterDTO;

public interface UserService {
    Result<Object> login(LoginDTO loginDTO);
    Result<Object> register(RegisterDTO registerDTO);
}
