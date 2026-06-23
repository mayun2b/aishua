# 简答题 AI 异步评分设计

## 背景

项目已经支持简答题画布作答，学生可以通过文字、手写画布和题图标注表达解题过程。但当前评分链路仍然停留在同步文本判定阶段：

- 练习模式的简答题仍然走 `AnswerJudgeSupport` 的文本完全匹配。
- 考试模式虽然每题有分值配置，但总分仍按“答对题数 / 总题数 * 试卷总分”折算，不能支持主观题部分得分。
- 画布和题图标注目前只参与回看，不参与评分。

本次设计聚焦于 `type = 5` 简答题，为练习和考试统一补齐 AI 异步评分能力。

## 目标

- 学生提交练习或考试时，先提交成功，再由后台异步完成简答题评分。
- 练习简答题只产出 `对 / 错`，不引入分值体系。
- 考试简答题按题目分值进行 AI 打分，支持部分得分。
- 支持画布作答和题图标注参与评分，不再只看文本框内容。
- AI 评分失败时标记为失败，不自动重试，由管理员手动重试。
- 评分状态、评分结果、失败原因、请求快照和响应快照可追踪、可排查。

## 非目标

- 不改造客观题判题逻辑。
- 不为普通用户开放“手动重试评分”入口。
- 不引入消息队列或独立评分服务。
- 不在第一期新增题目级评分细则编辑器，优先复用现有 `answer`、`analysis` 和统一规则。
- 不在第一期把练习侧统计改造成分值制。

## 方案选择

### 推荐方案：评分任务表 + 应用内 Worker

提交后先落答题记录，再按“每道简答题一条任务”写入评分任务表。后台定时 Worker 拉取待处理任务，准备评分素材，调用多模态 AI 评分，最后把结果回写到练习记录或考试记录中。

优点：

- 提交响应快，不阻塞学生交卷或交题。
- 状态清晰，天然支持 `待评分 / 评分中 / 评分成功 / 评分失败 / 手动重试`。
- 服务重启后任务不丢失。
- 失败任务和评分原始快照可审计，便于后续排查和重评。

代价：

- 需要新增任务表和状态汇总逻辑。
- 提交链路、记录详情、前端展示都要补状态字段。

### 备选方案：提交接口内直接 `@Async`

提交成功后在应用内直接起异步线程评分。

优点是改动小；缺点是服务重启会丢任务，失败后的可运维性差，不满足本次“失败后人工重试”的要求，因此不采用。

### 不采用方案：MQ / 独立评分服务

这类方案扩展性最好，但当前项目规模和现有基础设施没有必要先引入额外部署复杂度，本次不采用。

## 核心架构

### 提交流程

#### 练习

1. 保存所有题目的答题记录。
2. 客观题照常即时判对错并即时写统计。
3. 简答题不即时判分，写入 `待评分` 状态并创建评分任务。
4. 返回本次练习提交成功结果，同时返回简答题评分状态摘要。
5. 后台评分成功后再补写：
   - `exercise_record` 的 AI 评分结果；
   - `is_correct`；
   - 错题记录；
   - 掌握度和统计。

#### 考试

1. 保存所有答题记录。
2. 客观题即时判分并汇总客观题得分。
3. 简答题写入 `待评分` 状态并创建评分任务。
4. 提交接口立即返回“已提交，主观题评分中”。
5. 后台评分成功后按每题得分回写，并重新汇总整张试卷总分。

### 评分任务状态机

- `PENDING`：待评分
- `PROCESSING`：评分中
- `SUCCESS`：评分成功
- `FAILED`：评分失败

状态流转只允许：

`PENDING -> PROCESSING -> SUCCESS`

`PENDING -> PROCESSING -> FAILED`

手动重试时不覆盖旧任务，而是创建新任务，旧任务保留用于审计。

## 数据设计

### 1. 新增评分任务表

建议新增表：`ai_grading_task`

核心字段：

- `id`
- `biz_type`：`PRACTICE` / `EXAM`
- `biz_record_id`
  - 练习：`exercise_record.id`
  - 考试：`exam_record_question.id`
- `question_id`
- `user_id`
- `status`
- `trigger_source`：`SUBMIT` / `MANUAL_RETRY`
- `request_payload_json`
- `response_payload_json`
- `error_message`
- `retry_count`
- `max_retry_count`
- `locked_at`
- `started_at`
- `finished_at`
- `create_time`
- `update_time`
- `deleted`

说明：

- 虽然第一期不自动重试，但保留 `retry_count` 和 `max_retry_count` 字段，避免后续策略调整时再迁移。
- `request_payload_json` 和 `response_payload_json` 必须保留，便于核查“到底给模型送了什么”和“模型原始返回了什么”。

### 2. 练习记录补充字段

`exercise_record` 增加：

- `ai_grading_status`
- `ai_grading_confidence`
- `ai_grading_feedback`
- `ai_grading_detail_json`
- `ai_grading_error_message`
- `ai_graded_at`

说明：

- 练习不存分数，只存判定和说明。
- `is_correct` 在简答题上由 AI 评分成功后再写入。

### 3. 练习会话补充字段

`practice_session` 增加汇总状态字段：

- `grading_status`
- `pending_subjective_count`
- `failed_subjective_count`

用于列表页和详情页快速展示，不必每次遍历子记录现算。

### 4. 考试题目记录补充字段

`exam_record_question` 增加：

- `ai_grading_status`
- `ai_grading_confidence`
- `ai_grading_feedback`
- `ai_grading_detail_json`
- `ai_grading_error_message`
- `ai_graded_at`
- `full_score`
- `awarded_score`

说明：

- `awarded_score` 是考试简答题最终有效得分。
- `is_correct` 在考试简答题上只保留兼容用途，建议定义为“是否满分”，不再作为最终成绩口径。

### 5. 考试记录补充字段

`exam_record` 增加：

- `objective_score`
- `subjective_score`
- `grading_status`
- `pending_subjective_count`
- `failed_subjective_count`

最终总分仍然保存在原 `score` 字段中，但其来源改为：

`objective_score + subjective_score`

而不是当前的“答对题数折算”。

## AI 评分输入与输出

### 输入素材

每条评分任务在调用模型前，统一组装评分快照：

- 题目快照：题干、内容、标准答案、解析、题型、难度、图片说明、考试题满分。
- 学生文字答案。
- 学生手写画布图片。
- 题图标注复合图：
  - 原题图；
  - 标注层；
  - 合成后的只读评分图。
- 业务上下文：练习 / 考试、任务来源、记录 id。

### 图片处理策略

- 不直接把 MinIO `objectName` 交给模型。
- 由后端先通过 MinIO 服务读取对象内容。
- 题图标注采用“原图 + 标注层合成图”的形式参与评分。
- 画布和题图标注都作为证据输入，但不单独因为“画了图”加分，而是看图中是否提供了有效解题信息。

### 模型接入策略

本次评分不复用当前纯文本 `QwenChatClient` 的消息结构，而是新增专用多模态评分客户端。

原因：

- 当前 `QwenChatClient` 只支持 `role + content(text)`，无法承载图片输入。
- 评分链路需要更严格的结构化 JSON 输出约束。
- 评分链路需要独立模型、独立 token 限制和独立超时配置。

建议新增独立配置项，例如：

- `ai.qwen.grading-model`
- `ai.qwen.grading-max-tokens`
- `ai.qwen.grading-timeout`

不要直接复用当前聊天模型配置。

### 输出契约

#### 练习简答题

模型只返回：

- `isCorrect`
- `confidence`
- `feedback`
- `riskFlags`
- `reasoningSummary`

练习判对规则固定为：

- 结论必须正确；
- 关键步骤不能存在明显致命错误；
- 文本和画布冲突时判错；
- 证据不足或置信度过低时判错。

#### 考试简答题

模型返回：

- `awardedScore`
- `fullScore`
- `confidence`
- `dimensionScores`
- `feedback`
- `riskFlags`
- `reasoningSummary`
- `finalConclusion`
- `usedEvidence`

考试默认评分维度：

- 结论正确：40%
- 关键步骤：40%
- 图示 / 过程有效性：20%

第一期不新增题目级评分细则字段，统一使用默认维度和当前题目的 `answer`、`analysis` 作为评分依据。

## 写回规则

### 练习

- 评分成功：
  - 更新 `exercise_record.ai_grading_*`
  - 更新 `exercise_record.is_correct`
  - 更新 `practice_session.grading_status`
  - 补写错题、掌握度和统计
- 评分失败：
  - 更新 `exercise_record.ai_grading_status = FAILED`
  - 不写错题、不更新掌握度、不更新练习统计

### 考试

- 评分成功：
  - 更新 `exam_record_question.awarded_score`
  - 更新 `exam_record_question.ai_grading_*`
  - 重新汇总 `subjective_score`
  - 重新计算 `exam_record.score`
  - 更新 `exam_record.grading_status`
- 评分失败：
  - 更新题目状态为 `FAILED`
  - 试卷状态置为 `PARTIAL_FAILED`
  - 已有客观题得分保留

## 前端展示

### 练习

- 提交成功后，客观题照常展示结果。
- 简答题在详情页和记录页显示：
  - `评分中`
  - `回答正确`
  - `回答有误`
  - `评分失败`
- 不显示简答题分数。

### 考试

- 提交成功后立即展示：
  - 客观题即时得分
  - 主观题待评分数量
- 最终成绩页区分：
  - `objectiveScore`
  - `finalScore`
- 简答题待评分期间，`finalScore` 不应伪装成最终分数。

### 刷新策略

- 不上 WebSocket。
- 提交成功后如果存在待评分简答题，前端每 `3-5` 秒轮询详情接口。
- 轮询在下列条件停止：
  - 全部评分完成；
  - 存在评分失败；
  - 用户离开页面。

## 管理端

由于当前管理端没有练习记录管理页，本次建议新增独立的“AI 评分任务管理”页面，用于统一处理练习和考试失败任务。

最小能力：

- 按状态筛选任务。
- 查看任务来源（练习 / 考试）。
- 查看失败原因。
- 查看请求快照和响应快照。
- 仅对 `FAILED` 任务开放“重新评分”。

## 错误处理

- 模型调用异常、素材缺失、JSON 解析失败均记为 `FAILED`。
- `FAILED` 不自动重试。
- 手动重试新建任务，不覆盖旧任务。
- `PROCESSING` 超过阈值的僵尸任务，下一轮巡检时直接转 `FAILED`，等待人工重试。

## 测试与验收

### 后端

- 简答题任务创建测试。
- Worker 状态流转测试。
- 练习评分成功后统计延迟写入测试。
- 考试主观题部分得分汇总测试。
- 失败任务手动重试测试。

### 前端

- 提交后状态展示测试。
- 轮询停止条件测试。
- 管理端失败任务重试入口测试。

### 人工验收

- 练习提交后立即成功，简答题先显示 `评分中`，完成后变为 `正确 / 错误`。
- 练习评分失败时不污染错题本和掌握度。
- 考试提交后先显示客观题结果，最终总分在主观题评分完成后刷新。
- 画布作答且文本为空时，模型仍能基于手写过程评分。
- 文本和画布冲突时，练习判错，考试给出低置信度或扣分结果。
- 管理员可在失败任务页查看原因并手动重试。
