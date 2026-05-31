package com.dormrepair.ai.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class Nl2SqlResult {
    private String sql;
    private String explanation;
    private List<Map<String, Object>> data;
    private String source;
}
