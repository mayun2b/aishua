# Dify 接入 Spring Boot 后端实现方案

## 一、目标

当前系统需要实现：

用户登录后，在前端向 AI 提问，Spring Boot 后端自动识别当前登录用户，并调用 Dify 工作流。Dify 工作流中的 HTTP 节点需要访问 Spring Boot 后端接口，获取学生学情数据、考点数据、学习记录等。

核心要求：

1. 前端不直接调用 Dify。
2. 前端不保存 Dify API Key。
3. Dify 不直接使用用户登录 JWT。
4. Dify 访问后端时，使用服务级密钥鉴权。
5. 新增 Dify 内部接口，但复用原有 Service / Mapper / SQL 逻辑。
6. 内部接口只返回 AI 分析需要的数据，避免返回敏感信息。

---

## 二、整体调用链

```text
Vue 前端
  ↓ 携带用户登录 JWT
Spring Boot 后端
  ↓ 校验用户 JWT，解析当前 studentId
Spring Boot 调用 Dify Workflow API
  ↓ 传入 query、student_id、subject、grade、textbook_version
Dify 工作流
  ↓ HTTP 节点携带 X-DIFY-SERVICE-KEY
Spring Boot 内部接口 /internal/dify/**
  ↓ 校验服务密钥
  ↓ 查询学生学习数据
返回给 Dify
  ↓
Dify 生成回答
  ↓
Spring Boot 返回最终结果给 Vue
```

---

## 三、后端需要新增的内容

### 1. 新增 Dify 内部接口

建议新增接口路径：

```text
GET /internal/dify/practice/stats
GET /internal/dify/practice/tags
GET /internal/dify/study/records
```

接口用途：

| 接口 | 作用 |
|---|---|
| `/internal/dify/practice/stats` | 获取学生学情统计、知识点掌握情况 |
| `/internal/dify/practice/tags` | 获取某学科、教材版本下的考点目录 |
| `/internal/dify/study/records` | 获取学生近期学习记录或错题记录 |

注意：

不要重新写一套业务查询逻辑。  
这些 Controller 只负责接收 Dify 请求、校验参数、调用已有 Service。

推荐结构：

```text
controller
 ├── PracticeController.java              # 原有前端接口
 ├── StudyRecordController.java           # 原有前端接口
 └── internal
      └── DifyInternalController.java     # 新增 Dify 内部接口

service
 ├── PracticeStatsService.java            # 复用
 ├── KnowledgeTagService.java             # 复用
 └── StudyRecordService.java              # 复用

security
 ├── JwtAuthenticationFilter.java         # 原有用户 JWT 鉴权
 └── DifyServiceKeyFilter.java            # 新增 Dify 服务密钥鉴权
```

---

## 四、Dify 内部接口鉴权方案

### 1. 请求头规范

Dify HTTP 节点访问 Spring Boot 内部接口时，必须携带：

```text
X-DIFY-SERVICE-KEY: 后端配置的内部服务密钥
```

Spring Boot 后端需要校验该 Header。

校验失败时返回：

```text
401 Unauthorized
```

不要让 `/internal/dify/**` 裸奔。

---

### 2. 服务密钥配置

不要把服务密钥写死在代码里。

建议使用配置项：

```properties
dify.service-key=${DIFY_SERVICE_KEY}
```

本地开发可以放在 `application.properties` 或环境变量中。

正式环境建议放在以下位置之一：

- 服务器环境变量
- Docker Compose env
- 配置中心
- 密钥管理服务

---

## 五、Spring Security 配置要求

需要对接口做分层鉴权：

```text
/api/**                  继续走用户 JWT
/internal/dify/**        走 X-DIFY-SERVICE-KEY
```

不要把 `/internal/dify/**` 简单设置为 `permitAll`。

推荐逻辑：

1. 请求进入后端。
2. 如果路径是 `/internal/dify/**`：
   - 读取请求头 `X-DIFY-SERVICE-KEY`
   - 与后端配置的服务密钥比对
   - 正确则放行
   - 错误则返回 401
3. 如果路径是普通 `/api/**`：
   - 继续走原有 JWT 鉴权逻辑

---

## 六、内部接口参数要求

Dify 内部接口不要信任前端传来的 `student_id`。

正确来源：

```text
Vue 前端请求 Spring Boot
  ↓
Spring Boot 从用户 JWT 中解析 studentId
  ↓
Spring Boot 调用 Dify 时，把真实 studentId 传入 Dify
  ↓
Dify 再把这个 studentId 传给内部接口
```

也就是说，`student_id` 应该由后端注入，不应该让前端用户自己传。

---

## 七、内部接口设计

### 1. 学情统计接口

```text
GET /internal/dify/practice/stats
```

请求参数：

```text
student_id
subject_id 或 subject
grade
textbook_version
```

返回字段只保留 AI 分析需要的数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "studentId": 1,
    "subjectId": 1,
    "subjectName": "数学",
    "knowledgeMasteries": [
      {
        "tagId": 12,
        "tagName": "三角恒等变换",
        "subjectId": 1,
        "subjectName": "数学",
        "totalCount": 3,
        "correctCount": 0,
        "wrongCount": 3,
        "correctRate": 0,
        "masteryLevel": 0,
        "updateTime": "2026-05-27 10:00:00"
      }
    ]
  }
}
```

不要返回：

```text
手机号
密码
token
身份证
家庭信息
无关用户资料
```

---

### 2. 考点目录接口

```text
GET /internal/dify/practice/tags
```

请求参数：

```text
subject_id 或 subject
grade
textbook_version
```

返回建议：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "subjectName": "数学",
    "grade": "高一",
    "textbookVersion": "人教A版",
    "tags": [
      {
        "tagId": 12,
        "tagName": "三角恒等变换",
        "chapterName": "三角函数"
      }
    ]
  }
}
```

---

### 3. 学习记录接口

```text
GET /internal/dify/study/records
```

请求参数：

```text
student_id
subject_id 或 subject
limit
```

`limit` 必须限制最大值，例如最大 50，避免一次查询过多数据。

返回建议：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "studentId": 1,
    "records": [
      {
        "questionId": 1001,
        "tagName": "三角恒等变换",
        "isCorrect": false,
        "useTime": 120,
        "submitTime": "2026-05-27 10:00:00"
      }
    ]
  }
}
```

---

## 八、后端调用 Dify 的接口

新增或改造一个前端调用的 AI 接口，例如：

```text
POST /api/ai/chat
```

该接口给 Vue 调用，必须走用户 JWT。

前端请求示例：

```json
{
  "query": "帮我看看我哪里知识点薄弱",
  "subject": "数学",
  "grade": "高一",
  "textbook_version": "人教A版"
}
```

后端处理逻辑：

1. 校验用户 JWT。
2. 从 JWT 或登录上下文中获取当前 `studentId`。
3. 不允许直接相信前端传来的 `student_id`。
4. 组装 Dify Workflow 请求。
5. 调用 Dify API。
6. 获取 Dify 返回结果。
7. 返回给前端。

后端调用 Dify 时，请求头：

```text
Authorization: Bearer DIFY_APP_API_KEY
```

请求体大致为：

```json
{
  "inputs": {
    "student_id": "当前登录学生ID",
    "subject": "数学",
    "grade": "高一",
    "textbook_version": "人教A版"
  },
  "query": "帮我看看我哪里知识点薄弱",
  "response_mode": "blocking",
  "user": "当前登录学生ID"
}
```

`DIFY_APP_API_KEY` 只能放在 Spring Boot 服务端配置里，不能放前端。

---

## 九、Dify 工作流配置要求

Dify 开始节点保留这些输入：

```text
query
student_id
subject
grade
textbook_version
```

Dify 的 HTTP 节点访问后端内部接口时，统一配置：

```text
鉴权类型：API-Key
API 鉴权类型：自定义
Header 名称：X-DIFY-SERVICE-KEY
API Key：后端配置的内部服务密钥
```

HTTP 节点地址示例：

```text
http://host.docker.internal:8080/internal/dify/practice/stats
```

Query 参数示例：

```text
student_id = {{#开始.student_id#}}
subject = {{#开始.subject#}}
grade = {{#开始.grade#}}
textbook_version = {{#开始.textbook_version#}}
```

---

## 十、安全要求

| 安全项 | 要求 |
|---|---|
| `/internal/dify/**` 鉴权 | 必须校验 `X-DIFY-SERVICE-KEY` |
| 密钥存储 | 不允许写死代码，不允许放前端 |
| student_id 来源 | 必须由后端从 JWT 解析后注入 |
| 返回字段 | 只返回 AI 需要的数据 |
| 参数校验 | 必须校验 student_id、subject、grade、limit |
| 日志 | 不允许打印 token、Dify API Key、服务密钥 |
| 限流 | 建议对 `/internal/dify/**` 增加限流 |
| 错误处理 | 鉴权失败返回 401，参数错误返回 400 |

---

## 十一、实现优先级

### 第一阶段：先跑通链路

1. 新增 `/internal/dify/practice/stats`
2. 新增 `X-DIFY-SERVICE-KEY` 校验
3. Dify HTTP 节点配置服务密钥
4. Dify 成功获取学情数据
5. 学情分析 LLM 正常输出

### 第二阶段：完善功能

1. 新增 `/internal/dify/practice/tags`
2. 新增 `/internal/dify/study/records`
3. 优化 DTO 返回格式
4. 增加参数校验
5. 增加异常处理

### 第三阶段：安全加固

1. 密钥放环境变量
2. 内部接口加访问日志
3. 内部接口加限流
4. 返回字段最小化
5. 防止 student_id 越权查询

---

## 十二、给 Codex 的实现要求

请按以下要求实现：

1. 在 Spring Boot 项目中新增 Dify 内部接口 Controller。
2. 路径统一放在 `/internal/dify/**` 下。
3. 新增服务密钥鉴权机制，校验请求头 `X-DIFY-SERVICE-KEY`。
4. 服务密钥从配置文件或环境变量读取，不要写死在代码中。
5. 内部接口复用已有 Service / Mapper，不重复写业务查询逻辑。
6. 普通前端接口 `/api/**` 保持原有 JWT 鉴权逻辑。
7. 新增或改造 `/api/ai/chat`，由 Spring Boot 代理调用 Dify Workflow API。
8. `/api/ai/chat` 必须从当前登录用户上下文中获取 studentId，不允许信任前端传入的 `student_id`。
9. Dify Workflow API Key 只能放在后端配置里。
10. 所有内部接口返回 DTO 要最小化，只返回 AI 分析需要的数据。
11. 增加必要的参数校验和异常处理。
12. 不要在日志中打印用户 token、Dify API Key、`X-DIFY-SERVICE-KEY`。

---

## 十三、验收标准

实现后需要满足：

1. 前端只调用 Spring Boot，不直接调用 Dify。
2. 前端请求 `/api/ai/chat` 时携带用户 JWT。
3. Spring Boot 能解析当前登录学生 ID。
4. Spring Boot 能成功调用 Dify Workflow。
5. Dify HTTP 节点能通过 `X-DIFY-SERVICE-KEY` 调用 Spring Boot 内部接口。
6. 不携带或携带错误 `X-DIFY-SERVICE-KEY` 时，内部接口返回 401。
7. Dify 能拿到学生学情数据。
8. 学情分析节点能基于真实数据输出分析结果。
9. 原有前端接口不受影响。
10. 敏感密钥不出现在前端代码、接口响应和日志中。

---

## 十四、给 Codex 的提示语

可以直接把下面这句话发给 Codex：

```text
请根据这份方案，在我的 Spring Boot 项目中实现 Dify 内部接口、服务密钥鉴权、后端代理调用 Dify Workflow，并复用已有业务 Service，不要重复写查询逻辑。
```
