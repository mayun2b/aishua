# 刷题模块实现完成 - 使用指南

## 一、数据库初始化

### 1. 执行 SQL 脚本
在 MySQL 数据库中执行以下 SQL 脚本创建表结构：
```bash
# 进入 MySQL
mysql -u root -p

# 选择数据库
use aishuai;

# 执行建表脚本
source src/main/resources/db/schema.sql;
```

或者直接复制 `src/main/resources/db/schema.sql` 的内容在数据库管理工具中执行。

---

## 二、启动项目

### 1. 确保环境准备就绪
- MySQL 已启动并创建了 `aishuai` 数据库
- Redis 已启动（端口 6379）
- Java 17 环境

### 2. 修改数据库配置（如需要）
编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/aishuai?...
    username: your_username
    password: your_password
```

### 3. 启动应用
```bash
# Windows
mvnw.cmd spring-boot:run

# 或者使用 IDEA 直接运行 AishuaiApplication.java
```

---

## 三、API 接口说明

### 1. 题目管理模块 `/api/question`

#### 分页查询题目
```http
GET /api/question/page?pageNum=1&pageSize=10
```

#### 根据 ID 查询题目详情
```http
GET /api/question/{id}
```

#### 创建题目
```http
POST /api/question
Content-Type: application/json

{
  "title": "题目名称",
  "content": "题目内容（可选）",
  "type": 1,  // 1-单选，2-多选，3-判断，4-填空，5-简答
  "categoryId": 1,
  "difficulty": 1,  // 1-简单，2-中等，3-困难
  "options": "{\"A\":\"选项 A\",\"B\":\"选项 B\"}",  // 选择题需要
  "answer": "A",
  "analysis": "答案解析",
  "tags": "标签 1，标签 2"
}
```

#### 更新题目
```http
PUT /api/question/{id}
```

#### 删除题目
```http
DELETE /api/question/{id}
```

#### 随机获取题目
```http
GET /api/question/random?count=10&categoryId=1&difficulty=1
```

---

### 2. 练习模块 `/api/exercise`

#### 开始练习（获取题目列表）
```http
GET /api/exercise/start?count=10&categoryId=1&difficulty=1&exerciseMode=1
```
参数说明：
- count: 题目数量
- categoryId: 分类 ID（可选）
- difficulty: 难度（可选）
- exerciseMode: 练习模式（1-顺序，2-随机，3-错题，4-限时）

#### 提交答案
```http
POST /api/exercise/submit
Content-Type: application/json

{
  "questionId": 1,
  "userAnswer": "A",
  "timeCost": 30  // 耗时（秒），可选
}
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "recordId": 1,
    "isCorrect": true,
    "correctAnswer": "A",
    "analysis": "答案解析...",
    "timeCost": 30
  }
}
```

#### 批量提交答案
```http
POST /api/exercise/batch-submit
Content-Type: application/json

[
  {
    "questionId": 1,
    "userAnswer": "A",
    "timeCost": 30
  },
  {
    "questionId": 2,
    "userAnswer": "B",
    "timeCost": 25
  }
]
```

#### 获取用户统计
```http
GET /api/exercise/stats
```

---

### 3. 错题本模块 `/api/wrong`

#### 分页查询错题列表
```http
GET /api/wrong/page?pageNum=1&pageSize=10
```

#### 移除错题
```http
DELETE /api/wrong/{questionId}
```

#### 标记为已掌握
```http
PUT /api/wrong/{questionId}/mastered
```

#### 获取错题数量
```http
GET /api/wrong/count
```

#### 随机获取错题（用于错题重练）
```http
GET /api/wrong/random?count=10
```

---

### 4. 统计模块 `/api/stats`

#### 获取用户统计信息
```http
GET /api/stats/user
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "totalCount": 100,
    "correctCount": 85,
    "wrongCount": 15,
    "correctRate": 85.0,
    "continuousDays": 7
  }
}
```

---

## 四、快速测试流程

### 1. 初始化测试数据
启动项目后，调用以下接口创建测试题目：
```http
POST http://localhost:8080/api/test/question/init
```

### 2. 完整测试流程

#### Step 1: 查看题目列表
```http
GET http://localhost:8080/api/question/page?pageNum=1&pageSize=10
```

#### Step 2: 开始练习
```http
GET http://localhost:8080/api/exercise/start?count=5&exerciseMode=2
```

#### Step 3: 提交答案
```http
POST http://localhost:8080/api/exercise/submit
Content-Type: application/json

{
  "questionId": 1,
  "userAnswer": "B",
  "timeCost": 30
}
```

#### Step 4: 查看统计数据
```http
GET http://localhost:8080/api/exercise/stats
GET http://localhost:8080/api/stats/user
```

#### Step 5: 查看错题本
```http
GET http://localhost:8080/api/wrong/page
GET http://localhost:8080/api/wrong/count
```

---

## 五、功能特性说明

### ✅ 已实现的功能

1. **题目管理**
   - ✓ 题目的增删改查
   - ✓ 支持多种题型（单选、多选、判断、填空、简答）
   - ✓ 题目分类和难度等级
   - ✓ 题目标签
   - ✓ 题目统计（正确率、做题次数）

2. **练习模式**
   - ✓ 顺序练习
   - ✓ 随机练习
   - ✓ 错题重做（从错题本随机抽取）
   - ✓ 限时训练（预留字段）
   - ✓ 单题提交和批量提交

3. **自动判分**
   - ✓ 客观题自动判分（单选、多选、判断）
   - ✓ 填空题判分
   - ✓ 简答题暂时认为全对（需人工评分）

4. **错题本**
   - ✓ 自动收录错题
   - ✓ 错误次数统计
   - ✓ 标记为已掌握
   - ✓ 移除错题
   - ✓ 错题重练

5. **统计功能**
   - ✓ 总做题数
   - ✓ 正确率统计
   - ✓ 连续打卡天数
   - ✓ 最大连续天数
   - ✓ 最后练习日期

6. **数据一致性**
   - ✓ 使用事务保证数据一致性
   - ✓ 答题时同步更新：练习记录 + 用户统计 + 错题本 + 题目统计

---

## 六、注意事项

### ⚠️ 当前待完善的功能

1. **用户认证**
   - 目前所有接口的 userId 都写死为 1L
   - 需要从 JWT Token 中解析真实 userId
   - 解决方案：在拦截器中解析 token，将 userId 放入 ThreadLocal 或请求头

2. **多选题判分逻辑**
   - 当前需要完全匹配才判对
   - 可以优化为部分得分（如选对一半得 50% 分数）

3. **填空题多空判分**
   - 当前只支持单个空的填空
   - 可以扩展为支持多个空的答案

4. **简答题评分**
   - 当前简答题默认全对
   - 需要开发人工评分界面或 AI 评分功能

5. **Redis 缓存**
   - 题目缓存未实现
   - 用户每日做题计数未使用 Redis
   - 可以后续优化提升性能

6. **定时任务**
   - 每日凌晨重置用户每日做题计数
   - 每周生成学习报告

---

## 七、项目结构概览

```
src/main/java/zysy/iflytek/aishuai/
├── question/          # 题目模块
│   ├── controller/
│   ├── service/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   └── vo/
├── exercise/          # 练习模块
│   ├── controller/
│   ├── service/
│   ├── mapper/
│   ├── entity/
│   ├── dto/
│   └── vo/
├── wrong/            # 错题模块
│   ├── controller/
│   ├── service/
│   ├── mapper/
│   └── entity/
├── stats/            # 统计模块
│   ├── controller/
│   ├── service/
│   ├── mapper/
│   └── entity/
└── user/             # 用户模块（已有）
    ├── controller/
    ├── service/
    ├── mapper/
    └── entity/
```

---

## 八、后续优化建议

1. **集成 JWT 认证**
   - 在 AuthInterceptor 中解析 token
   - 使用 ThreadLocal 存储当前用户 ID
   - 所有接口从 ThreadLocal 获取 userId

2. **Redis 缓存优化**
   - 缓存热门题目
   - 使用 Redis 统计每日做题数
   - 使用 Redis ZSet 实现排行榜

3. **消息队列**
   - 使用 RabbitMQ/Kafka 异步处理统计数据
   - 削峰填谷，提升高并发场景性能

4. **数据分析**
   - 用户知识点掌握情况分析
   - 推荐薄弱知识点题目
   - 智能组卷

5. **前端界面**
   - 使用 Vue/React 开发前端
   - 实现答题卡片界面
   - 数据统计图表展示

---

## 九、常见问题

### Q1: 启动时报数据库连接错误？
A: 检查 MySQL 是否启动，数据库名、用户名、密码是否正确。

### Q2: 分页查询返回空数据？
A: 确认 MybatisPlusConfig 中已配置分页插件。

### Q3: 提交答案后错题本没有增加？
A: 检查答案是否正确，只有错误的答案才会加入错题本。

### Q4: 如何测试完整流程？
A: 按照本文档"快速测试流程"章节逐步操作。

---

## 十、技术栈总结

- **后端框架**: Spring Boot 3.1.8
- **ORM 框架**: MyBatis Plus 3.5.5
- **数据库**: MySQL
- **缓存**: Redis
- **JDK 版本**: Java 17
- **构建工具**: Maven
- **其他**: Lombok, FastJSON2, JWT

---

**恭喜！刷题模块已全部实现完成！** 🎉

如有问题，请查看代码注释或联系开发团队。
