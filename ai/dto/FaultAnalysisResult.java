package com.dormrepair.ai.dto;

import lombok.Data;
import java.util.List;

@Data
public class FaultAnalysisResult {
    private String title;
    private String category;
    private String description;
    private String advice;
    private String source;
}
