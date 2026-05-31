# dorm-repair-system
宿舍报修系统课程项目
基于Spring Boot 3 + Vue 3 + MySQL 8.0 + Docker + DeepSeek 4.0大模型开发的高校宿舍报修全流程管理系统，实现学生报修、管理员审核派单、维修人员接单处理、满意度评价闭环管理，支持AI智能分类派单与评价情感分析。
1.项目简介:
传统宿舍报修依赖线下登记、微信群沟通，存在信息混乱、进度不透明、派单无序、难以追溯等问题。本系统将报修业务数字化、流程化、智能化，面向学生、维修人员、管理员三类角色提供一站式服务，支持容器化一键部署，适配 PC 与移动端访问，满足高校后勤高效管理需求。
2.核心业务流程：
学生报修申请 → 管理员审核 → AI 智能派单 → 维修人员接单处理 → 完工确认 → 学生评价 → 数据统计分析
3.小组分工
王序苒：后端核心开发、Spring Boot 架构搭建、接口设计、AI 模块集成、Docker 容器化部署
高畅：数据库设计（ER 图 / 表结构 / DDL）、MyBatis-Plus 开发、统一异常处理、参数校验
张艺婧：前端 Vue3 开发、页面实现、接口联调、路由守卫、Tailwind CSS 样式、交互优化
胡田：需求分析、文档撰写、系统测试、AI 提示词设计、评价分析模块、答辩材料整理
4.技术栈:
后端:
Java 17 + Spring Boot 3.0
MyBatis-Plus 3.5.1
MySQL 8.0
Maven 3.9.6
JWT + Spring Security
SpringDoc OpenAPI (Swagger)
Redis 6.0+
Nginx
前端:
Vue 3 + Vite
Tailwind CSS
Axios
Vue Router
运维/部署:
Docker + Docker Compose
Git + GitHub
AI 相关:
AI 辅助编程：Claude Code
大模型 API：DeepSeek 4.0
功能：故障智能分类、自动派单、评价情感分析
5.如何运行:
(1)环境要求:
安装 Docker、Docker Compose
(2)启动步骤:
克隆项目到本地:
git clone 项目仓库地址
cd 项目根目录
一键启动所有服务（后端 + 前端 + MySQL+Redis）:
docker-compose up -d
访问系统:
前端地址：http://localhost:80
后端接口文档：http://localhost:8080/swagger-ui.html
停止服务:
docker-compose down
6.默认账号（测试用）:
学生：student/123456
维修人员：repair/123456
管理员：admin/123456
7.AI 功能说明
本系统 AI 模块由后端统一调用 DeepSeek 4.0 API，前端仅做展示，具备超时重试、错误降级策略，API 不可用时返回兜底结果。
(1)AI 智能工单分类与自动派单:
对报修描述做语义解析，自动识别故障类型（水电 / 空调 / 网络 / 门窗等）
按影响范围自动划分优先级（P1 紧急 / P2 高 / P3 中 / P4 低）
按技能、负载、距离智能匹配维修人员
超时未接单自动二次派单
(2)AI 评价情感分析
对学生文字评价做正向 / 中性 / 负向情感判定
自动提取关键词（速度快、态度差、未修好等）
生成维修人员服务质量评分与报表
差评自动分类并推送整改提醒
8.AI文档位置:
/docs/ai_module.md
包含：模型信息、提示词、调用流程、降级策略、费用控制方案
9.常见问题:
(1)Docker 启动失败
检查端口 80/8080/3306/6379 是否被占用
确认 Docker 与 Docker Compose 已正确安装
(2)前端无法访问后端接口
检查后端容器是否正常运行
确认 docker-compose.yml 中服务名与地址配置正确
(3)AI 功能不生效
检查 API 密钥配置是否正确
查看日志确认网络可访问大模型服务
触发降级策略时会使用默认规则派单
(4)数据库初始化失败
删除 mysql 数据卷后重新启动
检查/sql/schema.sql建表语句是否完整
(5)图片上传失败
检查挂载目录权限
确认文件大小与格式符合限制
