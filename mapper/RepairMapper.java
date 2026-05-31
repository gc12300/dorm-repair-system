package com.dormrepair.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormrepair.entity.Repair;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RepairMapper extends BaseMapper<Repair> {

    @Update("UPDATE repair SET status=#{status}, handle_desc=#{handleDesc}, admin_id=#{adminId} WHERE id=#{id}")
    int updateHandle(Long id, Long adminId, String status, String handleDesc);

    @Update("UPDATE repair SET rating=#{rating}, review=#{review} WHERE id=#{id}")
    int updateRating(Long id, Integer rating, String review);
}
