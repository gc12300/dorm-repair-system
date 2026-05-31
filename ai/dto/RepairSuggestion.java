package com.dormrepair.ai.dto;

import lombok.Data;
import java.util.List;

@Data
public class RepairSuggestion {
    private String plan;
    private List<String> tools;
    private Double estimatedHours;
    private String priority;
    private String source;
}
