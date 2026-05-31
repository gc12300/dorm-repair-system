package com.dormrepair.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dormitory")
public class Dormitory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String building;
    private String roomNo;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
