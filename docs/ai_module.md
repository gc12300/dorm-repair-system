# AI 报修助手模块文档

## 基本信息

| 项目 | 内容 |
|------|------|
| 模型名称 | DeepSeek-v4-flash (API 调用使用 `deepseek-chat`) |
| API 提供商 | DeepSeek (深度求索) |
| API 地址 | `https://api.deepseek.com/v1/chat/completions` |
| 认证方式 | Bearer Token (API Key) |
| 请求协议 | HTTPS POST, JSON 格式 |
| 超时设置 | 连接超时 10s, 读取超时 30s |
| 重试策略 | 最多重试 2 次, 间隔 1s |
| 每日限额 | 每用户每天 10 次 |

---

## 功能概述

AI 报修助手为三种用户角色提供差异化 AI 能力：

```
学生 ──→ 故障智能分析 ──→ 生成规范报修标题、分类、补充描述、临时处理建议
维修人员 ──→ 维修方案推荐 ──→ 推荐维修步骤、工具清单、预估工时、优先级
管理员 ──→ 自然语言查询 ──→ NL2SQL 转换 + SQL 安全执行 + 数据返回
```

---

## API 接口

### 1. 学生端 - 故障智能分析

```
POST /api/ai/student/analyze
Header: Authorization: Bearer <token>
```

**请求体：**
```json
{
  "query": "我们宿舍卫生间的水龙头一直漏水，关不紧，地上全是水"
}
```

**响应示例（AI 成功）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "title": "卫生间水龙头漏水",
    "category": "水电",
    "description": "宿舍卫生间水龙头密封失效导致持续漏水，水龙头无法完全关闭，地面已有积水，影响正常使用并存在滑倒安全隐患，需尽快安排维修人员更换密封圈或水龙头。",
    "advice": "1. 立即关闭洗手池下方的角阀停止供水\n2. 用拖把清理地面积水防止滑倒\n3. 在水龙头下方放置水桶接住滴水",
    "source": "ai"
  }
}
```

**降级响应示例（API 不可用时）：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "title": "宿舍故障报修",
    "category": "公共设施",
    "description": "我们宿舍卫生间的水龙头一直漏水...",
    "advice": "1. 确保人身安全，远离危险区域\n2. 关闭相关水电阀门或电源\n3. 联系宿管人员现场查看",
    "source": "fallback"
  }
}
```

### 2. 维修人员端 - 维修方案推荐

```
POST /api/ai/worker/suggest
Header: Authorization: Bearer <token>
```

**请求体：**
```json
{
  "query": "卫生间水龙头密封失效导致持续漏水，水龙头无法完全关闭"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "plan": "1. 关闭供水总阀\n2. 拆卸损坏水龙头\n3. 检查水管接口螺纹状态\n4. 更换同规格陶瓷阀芯水龙头\n5. 缠绕生料带密封安装\n6. 打开阀门测试密封性",
    "tools": ["活动扳手", "生料带", "陶瓷阀芯水龙头", "手电筒", "抹布"],
    "estimatedHours": 0.8,
    "priority": "中",
    "source": "ai"
  }
}
```

### 3. 管理员端 - 自然语言数据查询 (NL2SQL)

```
POST /api/ai/admin/query
Header: Authorization: Bearer <token>
```

**请求体：**
```json
{
  "query": "帮我查本周未处理的报修有多少条"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sql": "SELECT COUNT(*) AS count FROM repair WHERE status = 'PENDING' AND create_time >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)",
    "explanation": "查询本周内状态为待处理的报修总数",
    "data": [
      { "count": 8 }
    ],
    "source": "ai"
  }
}
```

### 4. 查询剩余额度

```
GET /api/ai/quota
Header: Authorization: Bearer <token>
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dailyLimit": 10,
    "remaining": 7
  }
}
```

---

## 提示词设计思路

### 学生故障分析提示词

```
角色定位：高校宿舍报修助手
输入：学生口语化故障描述
输出要求：严格 JSON 格式，4 个字段
  - title: 规范标题 (≤20字)
  - category: 从预定义分类中匹配 (水电/家具/网络/公共设施)
  - description: 口语→规范描述增强 (50-150字)
  - advice: 安全导向的临时处理建议 (2-3条)

设计要点：
  - 低温度参数 (0.3) 保证输出稳定性
  - JSON-only 指令避免 markdown 包装
  - 分类枚举约束防止模型自由发挥
```

### 维修方案推荐提示词

```
角色定位：经验丰富的高校后勤维修工程师
输入：报修故障描述
输出要求：严格 JSON 格式，4 个字段
  - plan: 分步骤维修方案
  - tools: 工具清单数组
  - estimatedHours: 数字型预估工时
  - priority: 四级优先级分类

设计要点：
  - 专业角色赋予提升方案可信度
  - 工具清单数组格式便于前端渲染
  - 优先级分级与报修状态联动
```

### 管理员 NL2SQL 提示词

```
角色定位：MySQL 数据库查询助手
输入：管理员自然语言查询
输出要求：严格 JSON 格式，2 个字段
  - sql: 可执行的 SELECT 语句
  - explanation: 中文查询意图说明

设计要点：
  - 提供完整数据库 schema 上下文
  - 枚举值说明 (role, status 取值)
  - 时间函数引导 (CURDATE、DATE_SUB)
  - 安全约束：仅允许 SELECT，禁止写操作
  - 服务端二次校验：非 SELECT 语句拒绝执行
```

---

## 调用流程

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐     ┌───────────┐
│ 前端请求  │────→│ AuthInterceptor│────→│  AiController │────→│ RateLimiter│
│ (含JWT)  │     │ 提取userId/role│     │  角色校验     │     │ 次数检查   │
└──────────┘     └──────────────┘     └──────────────┘     └─────┬─────┘
                                                                  │
                                                    ┌─────────────┘
                                                    │ 通过
                                                    ▼
                                             ┌──────────────┐
                                             │   AiService   │
                                             │  构造提示词    │
                                             └──────┬───────┘
                                                    │
                                                    ▼
                                             ┌──────────────┐
                                             │  RestTemplate │ ← 超时10s/30s
                                             │  POST DeepSeek│ ← 重试2次
                                             └──────┬───────┘
                                                    │
                                          ┌─────────┴─────────┐
                                          │ API 可用?          │
                                          ├─────────┬─────────┤
                                          │ 是      │ 否      │
                                          ▼         ▼         │
                                    ┌──────────┐ ┌──────────┐│
                                    │ 解析JSON  │ │ fallback()│
                                    │ 返回结果  │ │ 返回兜底  │
                                    └──────────┘ └──────────┘│
                                          │         │         │
                                          └────┬────┘         │
                                               ▼               │
                                        ┌──────────────┐      │
                                        │  返回前端     │←─────┘
                                        └──────────────┘
```

---

## 成本控制与限流方案

### 1. 用户级限流
- 实现方式：内存 `ConcurrentHashMap<userId, count>`，每日 0 点定时清空
- 限制额度：每用户每天 10 次 AI 调用
- 超限响应：HTTP 200 + code=429，提示 "今日AI调用次数已达上限（10次），请明日再试"

### 2. 模型参数优化
- `temperature=0.3`：低随机性，减少 token 浪费
- `max_tokens=1024`：限制单次最大输出，防止异常长回复

### 3. 超时与重试
- 连接超时 10s，读取超时 30s，防止长时间挂起
- 最多重试 2 次，间隔 1s，避免频繁重试造成 API 费用叠加

### 4. 降级策略
- API 不可用时返回预定义的兜底文案（`source: "fallback"`）
- 不抛 HTTP 500 错误，保证用户体验连续性
- 兜底文案标注来源，便于用户识别

### 5. 安全防护
- NL2SQL 仅允许 SELECT 语句，服务端二次校验
- 写操作语句（INSERT/UPDATE/DELETE/DROP/ALTER）自动拒绝
- 数据库查询异常时返回空结果 + 友好提示

---

## 项目文件清单

```
backend/src/main/java/com/dormrepair/ai/
├── AiConfig.java              # DeepSeek API 配置 + RestTemplate Bean
├── AiService.java             # AI 核心服务（提示词 + API 调用 + 降级）
├── AiController.java          # AI 接口控制器（角色鉴权 + 限流）
├── RateLimiter.java           # 每日调用次数限流器
└── dto/
    ├── AiRequest.java         # 统一请求 DTO
    ├── FaultAnalysisResult.java  # 故障分析结果
    ├── RepairSuggestion.java     # 维修方案推荐结果
    └── Nl2SqlResult.java         # NL2SQL 查询结果
```
