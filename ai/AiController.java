package com.dormrepair.ai;

import com.dormrepair.ai.dto.*;
import com.dormrepair.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final RateLimiter rateLimiter;

    public AiController(AiService aiService, RateLimiter rateLimiter) {
        this.aiService = aiService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/student/analyze")
    public Result<?> studentAnalyze(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"STUDENT".equals(role)) {
            return Result.error(403, "仅学生用户可访问此接口");
        }
        Long userId = (Long) request.getAttribute("userId");
        if (!rateLimiter.tryAcquire(userId)) {
            return Result.error(429, "今日AI调用次数已达上限(" + RateLimiter.DAILY_LIMIT + "次)，请明日再试");
        }
        return Result.success(aiService.analyzeFault(req.getQuery()));
    }

    @PostMapping("/worker/suggest")
    public Result<?> workerSuggest(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅维修人员可访问此接口");
        }
        Long userId = (Long) request.getAttribute("userId");
        if (!rateLimiter.tryAcquire(userId)) {
            return Result.error(429, "今日AI调用次数已达上限(" + RateLimiter.DAILY_LIMIT + "次)，请明日再试");
        }
        return Result.success(aiService.suggestRepair(req.getQuery()));
    }

    @PostMapping("/admin/query")
    public Result<?> adminQuery(@Valid @RequestBody AiRequest req, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "仅管理员可访问此接口");
        }
        Long userId = (Long) request.getAttribute("userId");
        if (!rateLimiter.tryAcquire(userId)) {
            return Result.error(429, "今日AI调用次数已达上限(" + RateLimiter.DAILY_LIMIT + "次)，请明日再试");
        }
        return Result.success(aiService.nl2SqlQuery(req.getQuery()));
    }

    @GetMapping("/quota")
    public Result<?> getQuota(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        int remaining = rateLimiter.getRemaining(userId);
        return Result.success(new java.util.HashMap<String, Object>() {{
            put("dailyLimit", RateLimiter.DAILY_LIMIT);
            put("remaining", remaining);
        }});
    }
}
