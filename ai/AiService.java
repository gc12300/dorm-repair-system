package com.dormrepair.ai;

import com.dormrepair.ai.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {

    private final RestTemplate restTemplate;
    private final AiConfig.DeepSeekProperties ds;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 1000;

    public AiService(RestTemplate restTemplate, AiConfig.DeepSeekProperties ds,
                     JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.ds = ds;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    // ========== 学生：故障智能分析 ==========

    public FaultAnalysisResult analyzeFault(String query) {
        String systemPrompt = "你是一个高校宿舍报修助手。学生描述了故障情况，请分析并返回JSON格式结果。\n" +
                "\n" +
                "要求：\n" +
                "1. title: 生成规范简洁的报修标题（不超过20字）\n" +
                "2. category: 从 [\"水电\", \"家具\", \"网络\", \"公共设施\"] 中选择最匹配的分类\n" +
                "3. description: 将学生的口语化描述补充完善为规范的故障描述（50-150字）\n" +
                "4. advice: 给出2-3条临时处理建议，如关闭阀门、断电等安全措施\n" +
                "仅返回JSON，不要包含markdown代码块标记：\n" +
                "{\"title\":\"...\",\"category\":\"...\",\"description\":\"...\",\"advice\":\"...\"}";

        String response = callDeepSeek(systemPrompt, query);

        if (response == null) {
            return fallbackFaultAnalysis(query);
        }

        try {
            FaultAnalysisResult result = objectMapper.readValue(response, FaultAnalysisResult.class);
            result.setSource("ai");
            return result;
        } catch (Exception e) {
            return fallbackFaultAnalysis(query);
        }
    }

    private FaultAnalysisResult fallbackFaultAnalysis(String query) {
        FaultAnalysisResult r = new FaultAnalysisResult();
        r.setTitle("宿舍故障报修");
        r.setCategory("公共设施");
        r.setDescription(query.length() > 100 ? query.substring(0, 100) + "..." : query);
        r.setAdvice("1. 确保人身安全，远离危险区域\\n2. 关闭相关水电阀门或电源\\n3. 联系宿管人员现场查看");
        r.setSource("fallback");
        return r;
    }

    // ========== 维修人员：维修方案推荐 ==========

    // 维修人员：维修方案推荐
    public RepairSuggestion suggestRepair(String description) {
        String systemPrompt = "你是一个经验丰富的高校后勤维修工程师。请根据报修故障描述，推荐维修方案。\n" +
                "\n" +
                "返回JSON格式：\n" +
                "1. plan: 详细维修方案（步骤说明，100-200字）\n" +
                "2. tools: 所需工具清单，数组格式如 [\"工具1\", \"工具2\"]\n" +
                "3. estimatedHours: 预估工时（数字，单位小时，如1.5）\n" +
                "4. priority: 优先级，从 [\"低\", \"中\", \"高\", \"紧急\"] 中选择\n" +
                "仅返回JSON，不要包含markdown代码块标记：\n" +
                "{\"plan\":\"...\",\"tools\":[\"...\"],\"estimatedHours\":0,\"priority\":\"...\"}";

        String response = callDeepSeek(systemPrompt, description);

        if (response == null) {
            return fallbackRepairSuggestion(description);
        }

        try {
            RepairSuggestion result = objectMapper.readValue(response, RepairSuggestion.class);
            result.setSource("ai");
            return result;
        } catch (Exception e) {
            return fallbackRepairSuggestion(description);
        }
    }

    private RepairSuggestion fallbackRepairSuggestion(String description) {
        RepairSuggestion r = new RepairSuggestion();
        r.setPlan("1. 先检查故障是否为简单松动问题；2. 准备对应工具进行初步处理；3. 如无法解决，请上报管理员。");
        r.setTools(Arrays.asList("螺丝刀", "钳子", "绝缘胶带"));
        r.setEstimatedHours(0.5);
        r.setPriority("中");
        r.setSource("fallback");
        return r;
    }


    // ========== 管理员：自然语言数据查询 ==========

    public Nl2SqlResult nl2SqlQuery(String query) {
        String systemPrompt = "你是一个MySQL数据库查询助手。数据库名称为dorm_repair，包含以下表：\n" +
                "\n" +
                "user (id, username, password, role, create_time, update_time)\n" +
                "  - role取值: STUDENT, ADMIN\n" +
                "\n" +
                "student (id, user_id, student_no, name, phone, dormitory_id, create_time, update_time)\n" +
                "\n" +
                "admin (id, user_id, name, phone, create_time, update_time)\n" +
                "\n" +
                "dormitory (id, building, room_no, create_time)\n" +
                "\n" +
                "repair (id, student_id, dormitory_id, title, description, status, admin_id, handle_desc, create_time, update_time)\n" +
                "  - status取值: 待处理, 处理中, 已完成, 已取消\n" +
                "\n" +
                "请将用户的自然语言查询转换为单条只读SELECT语句。要求：\n" +
                "1. 只生成SELECT语句，禁止INSERT/UPDATE/DELETE/DROP/ALTER\n" +
                "2. sql字段包含完整可执行的SQL\n" +
                "3. explanation字段用中文简要说明查询意图\n" +
                "4. 若涉及时间范围，使用CURDATE()、DATE_SUB等函数\n" +
                "仅返回JSON，不要包含markdown代码块标记：\n" +
                "{\"sql\":\"SELECT ...\",\"explanation\":\"...\"}";

        String response = callDeepSeek(systemPrompt, query);

        if (response == null) {
            return fallbackNl2Sql(query);
        }

        try {
            Map<String, Object> map = objectMapper.readValue(response,
                    new TypeReference<Map<String, Object>>() {});
            String sql = (String) map.get("sql");
            String explanation = (String) map.get("explanation");

            if (sql == null || !sql.trim().toUpperCase().startsWith("SELECT")) {
                return fallbackNl2Sql(query);
            }

            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);

            Nl2SqlResult result = new Nl2SqlResult();
            result.setSql(sql);
            result.setExplanation(explanation);
            result.setData(data);
            result.setSource("ai");
            return result;
        } catch (Exception e) {
            return fallbackNl2Sql(query);
        }
    }

    private Nl2SqlResult fallbackNl2Sql(String query) {
        Nl2SqlResult r = new Nl2SqlResult();
        r.setSql("-- AI生成失败，请手动编写SQL");
        r.setExplanation("AI服务暂时不可用，请通过管理后台手动查询数据");
        r.setData(Collections.emptyList());
        r.setSource("fallback");
        return r;
    }

    // ========== 通用DeepSeek调用（含超时、重试、降级） ==========

    private String callDeepSeek(String systemPrompt, String userMessage) {
        String url = ds.getBaseUrl() + "/v1/chat/completions";
        Map<String, Object> body = new HashMap<>();
        body.put("model", ds.getModel());
        body.put("temperature", 0.3);
        body.put("max_tokens", 1024);
        body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        ));

        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("Authorization", "Bearer " + ds.getApiKey());
                headers.set("Content-Type", "application/json");

                org.springframework.http.HttpEntity<Map<String, Object>> entity =
                        new org.springframework.http.HttpEntity<>(body, headers);

                Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);
                if (response == null) continue;

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices == null || choices.isEmpty()) continue;

                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                String content = (String) message.get("content");
                if (content != null) {
                    content = content.trim();
                    if (content.startsWith("```")) {
                        content = content.replaceAll("```\\w*\\n?", "").replaceAll("\\n```", "");
                    }
                    return content;
                }
            } catch (RestClientException e) {
                if (attempt == MAX_RETRIES) return null;
                try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ignored) {}
            }
        }
        return null;
    }
}
