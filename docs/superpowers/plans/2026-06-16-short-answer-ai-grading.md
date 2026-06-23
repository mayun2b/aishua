# 简答题 AI 异步评分开发计划

> **给后续执行者：** 按任务顺序推进，步骤使用 `- [ ]` 复选框跟踪。优先在独立分支执行，不要直接在 `main` 上实施。

**目标：** 为练习和考试中的简答题接入 AI 异步评分；练习只判断对错，考试支持按题得分；提交先成功，后台几秒后补评分；失败任务仅允许管理员手动重试。

**架构：** 采用“答题记录先落库 + 每道简答题一条评分任务 + 应用内 Worker 异步处理”的模式。评分任务统一落到 `ai_grading_task`，Worker 负责准备文字、画布和题图标注素材并调用专用多模态评分客户端。练习评分成功后再补写统计和错题；考试评分成功后再重算试卷总分。

**技术栈：** Vue 3、Vue Router、现有 Axios API 封装、Spring Boot、MyBatis-Plus、MySQL 脚本 `src/main/resources/db/aishua.sql`、MinIO、现有 Qwen 接入的专用扩展。

---

## 接口与路由建议

### 用户侧

- 练习提交接口沿用现有接口，只扩展返回字段：
  - `gradingStatus`
  - `pendingSubjectiveCount`
  - `failedSubjectiveCount`
- 考试提交接口沿用现有接口，只扩展返回字段：
  - `objectiveScore`
  - `finalScore`
  - `gradingStatus`
  - `pendingSubjectiveCount`
  - `failedSubjectiveCount`
- 练习记录详情和考试记录详情沿用现有接口，只扩展评分状态和反馈字段，供前端轮询。

### 管理端

建议新增：

- `GET /admin/ai-grading/tasks`
- `GET /admin/ai-grading/tasks/{id}`
- `POST /admin/ai-grading/tasks/{id}/retry`

建议新增前端路由：

- `/admin/ai-grading`

这样不需要把练习失败重试硬塞进考试管理页，也不会打散现有导航结构。

---

## 文件结构

- 修改 `aishua-master/src/main/resources/db/aishua.sql`
  - 为练习记录、练习会话、考试记录、考试题目记录补充评分字段。
  - 新增 `ai_grading_task` 表。
- 新增后端 AI 评分任务模块：
  - `aishua-master/src/main/java/zysy/iflytek/aishua/modules/ai/entity/AiGradingTask.java`
  - `aishua-master/src/main/java/zysy/iflytek/aishua/modules/ai/mapper/AiGradingTaskMapper.java`
  - `aishua-master/src/main/java/zysy/iflytek/aishua/modules/ai/service/AiGradingTaskService.java`
  - `aishua-master/src/main/java/zysy/iflytek/aishua/modules/ai/service/impl/AiGradingTaskServiceImpl.java`
  - `aishua-master/src/main/java/zysy/iflytek/aishua/modules/ai/service/impl/AiGradingWorker.java`
  - `aishua-master/src/main/java/zysy/iflytek/aishua/modules/ai/support/ShortAnswerGradingClient.java`
  - `aishua-master/src/main/java/zysy/iflytek/aishua/modules/ai/support/ShortAnswerGradingPromptBuilder.java`
- 修改后端练习与考试主链路：
  - `PracticeServiceImpl.java`
  - `PracticeSubmissionStatsUpdater.java`
  - `ExamServiceImpl.java`
  - 相关 entity / dto / vo / controller
- 修改 MinIO 读取能力：
  - `aishua-master/src/main/java/zysy/iflytek/aishua/common/minio/service/MinioService.java`
- 新增管理端评分任务页：
  - `aishua-web/src/modules/admin/api/aiGrading.js`
  - `aishua-web/src/modules/admin/views/AdminAiGradingTaskView.vue`
  - 修改 `aishua-web/src/router/index.js`
  - 修改 `aishua-web/src/router/navigation.js`
  - 修改 `aishua-web/src/modules/admin/views/AdminDashboardView.vue`
- 修改用户端练习与考试页面：
  - `PracticeSessionView.vue`
  - `PracticeRecordsView.vue`
  - `PracticeRecordDetailView.vue`
  - `ExamSessionView.vue`
  - `ExamRecordListView.vue`
  - `ExamRecordDetailView.vue`

---

## Task 1：数据库与领域模型落地

**文件：**

- 修改：`aishua-master/src/main/resources/db/aishua.sql`
- 修改：`ExerciseRecord.java`
- 修改：`PracticeSession.java`
- 修改：`ExamRecord.java`
- 修改：`ExamRecordQuestion.java`
- 新增：`AiGradingTask.java`

- [ ] **Step 1：新增评分任务表**

在 `aishua.sql` 中新增 `ai_grading_task`，字段至少包括：

- `biz_type`
- `biz_record_id`
- `question_id`
- `user_id`
- `status`
- `trigger_source`
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

- [ ] **Step 2：给练习记录和练习会话补评分字段**

为 `exercise_record` 增加：

- `ai_grading_status`
- `ai_grading_confidence`
- `ai_grading_feedback`
- `ai_grading_detail_json`
- `ai_grading_error_message`
- `ai_graded_at`

为 `practice_session` 增加：

- `grading_status`
- `pending_subjective_count`
- `failed_subjective_count`

- [ ] **Step 3：给考试记录补评分字段**

为 `exam_record_question` 增加：

- `ai_grading_status`
- `ai_grading_confidence`
- `ai_grading_feedback`
- `ai_grading_detail_json`
- `ai_grading_error_message`
- `ai_graded_at`
- `full_score`
- `awarded_score`

为 `exam_record` 增加：

- `objective_score`
- `subjective_score`
- `grading_status`
- `pending_subjective_count`
- `failed_subjective_count`

- [ ] **Step 4：同步 Java 实体**

为所有新增字段补齐 entity 属性，保持与表结构一致。评分状态建议统一抽成常量或枚举，不要在业务代码里散落魔法值。

- [ ] **Step 5：检查现有 VO / DTO 缺口**

梳理哪些返回对象需要暴露评分状态：

- `PracticeBatchSubmitResultVO`
- `PracticeExerciseRecordVO`
- `ExamSubmitResultVO`
- `ExamRecordSummaryVO`
- `ExamRecordQuestionVO`

在计划中明确后续任务会补这些字段。

---

## Task 2：提交链路重构与评分任务入队

**文件：**

- 修改：`PracticeServiceImpl.java`
- 修改：`ExamServiceImpl.java`
- 新增或修改：相关 VO / DTO / controller

- [ ] **Step 1：练习提交拆成“保存记录”和“统计写入”两段**

对客观题保持即时判题和即时统计。  
对简答题：

- 保存 `user_answer`
- 标记 `ai_grading_status = PENDING`
- 创建 `ai_grading_task`
- 暂不写 `is_correct`
- 暂不写错题和掌握度统计

- [ ] **Step 2：考试提交拆成“客观题即时结算”和“主观题延迟结算”**

考试提交时：

- 客观题即时写 `is_correct`
- 累加 `objective_score`
- 简答题写 `PENDING` 并创建任务
- `score` 先仅代表当前可确认得分，或通过 `objective_score` 单独返回
- 若存在待评分简答题，整张卷子的 `grading_status` 置为 `PENDING`

- [ ] **Step 3：统一创建任务的入口**

抽一个共享方法，保证练习和考试创建任务时都写同样的字段：

- 业务类型
- 业务记录 id
- 用户 id
- 题目 id
- 任务来源 `SUBMIT`
- 评分请求快照

- [ ] **Step 4：提交接口返回评分摘要**

练习和考试提交接口都补充返回：

- `gradingStatus`
- `pendingSubjectiveCount`
- `failedSubjectiveCount`

考试提交接口额外补：

- `objectiveScore`
- `finalScore`（待评分时可为空）

---

## Task 3：评分 Worker 与任务锁

**文件：**

- 新增：`AiGradingTaskService.java`
- 新增：`AiGradingTaskServiceImpl.java`
- 新增：`AiGradingWorker.java`
- 新增：`AiGradingTaskMapper.java`

- [ ] **Step 1：定义任务抓取规则**

Worker 每轮只抓 `PENDING` 任务，按 `create_time` 升序处理。抓取时要做行级锁或乐观更新，避免多实例重复消费。

- [ ] **Step 2：定义状态流转和锁超时**

任务开始处理时写：

- `status = PROCESSING`
- `locked_at`
- `started_at`

任务完成时写：

- `finished_at`
- `status = SUCCESS / FAILED`

对长时间卡在 `PROCESSING` 的任务，不自动重跑，巡检时直接转为 `FAILED`，等待人工重试。

- [ ] **Step 3：保留失败原因**

模型调用异常、素材获取失败、结果 JSON 解析失败，都要写 `error_message`。  
不要把所有失败都压成同一条通用提示。

- [ ] **Step 4：实现手动重试策略**

重试时：

- 不覆盖旧任务；
- 新建一条任务；
- `trigger_source = MANUAL_RETRY`；
- 新任务重新走完整评分流程。

---

## Task 4：评分素材准备与多模态 AI 客户端

**文件：**

- 新增：`ShortAnswerGradingClient.java`
- 新增：`ShortAnswerGradingPromptBuilder.java`
- 修改：`MinioService.java`
- 可选新增：图片合成辅助类
- 修改：`QwenAiProperties.java`

- [ ] **Step 1：扩展 MinIO 读取能力**

为后端增加直接读取对象字节流或字节数组的方法。  
评分链路不要依赖前端预签名 URL，也不要要求模型去访问 `localhost:9000`。

- [ ] **Step 2：解析用户答案中的画布与标注信息**

复用现有：

- `[[canvas:...]]`
- `[[image-annotations:...]]`

解析出：

- 文字答案
- 手写画布对象
- 每张题图对应的标注层对象

- [ ] **Step 3：合成评分图片**

对每张带标注的题图：

- 下载原图；
- 下载标注层；
- 在后端合成出只读评分图；
- 作为模型评分证据。

手写画布则直接作为独立证据图输入。

- [ ] **Step 4：新增专用评分客户端**

不要复用当前纯文本聊天消息结构。  
新增评分客户端，并为其单独配置：

- `grading-model`
- `grading-max-tokens`
- `grading-timeout`

评分客户端只接受结构化输入并要求结构化 JSON 输出。

- [ ] **Step 5：固化输出契约**

练习题输出：

- `isCorrect`
- `confidence`
- `feedback`
- `riskFlags`
- `reasoningSummary`

考试题输出：

- `awardedScore`
- `fullScore`
- `confidence`
- `dimensionScores`
- `feedback`
- `riskFlags`
- `reasoningSummary`
- `finalConclusion`
- `usedEvidence`

若结构不合法，直接判任务失败，不做宽松兜底。

---

## Task 5：评分结果回写与练习统计补写

**文件：**

- 修改：`PracticeServiceImpl.java`
- 修改：`PracticeSubmissionStatsUpdater.java`
- 新增或修改：练习相关 VO

- [ ] **Step 1：练习评分成功后的回写**

回写：

- `exercise_record.ai_grading_*`
- `exercise_record.is_correct`
- `exercise_record.ai_graded_at`

并根据 `is_correct` 再补执行：

- `practiceSubmissionStatsUpdater.applyAnswerStats(...)`
- `upsertWrongQuestion(...)`

- [ ] **Step 2：练习评分失败后的处理**

如果任务失败：

- 只更新 `exercise_record.ai_grading_status = FAILED`
- 不写 `is_correct`
- 不更新错题本
- 不更新掌握度
- 不更新练习统计

- [ ] **Step 3：练习会话汇总状态**

在练习会话层汇总：

- 还剩多少主观题待评分
- 是否存在失败题
- 当前整体 `grading_status`

这样记录列表和详情页可以直接展示。

---

## Task 6：考试得分回写与总分重算

**文件：**

- 修改：`ExamServiceImpl.java`
- 修改：考试相关 VO / DTO / controller

- [ ] **Step 1：考试题目级回写**

简答题评分成功后回写：

- `awarded_score`
- `full_score`
- `ai_grading_*`
- `is_correct`（建议定义为“是否满分”）

- [ ] **Step 2：试卷级重算**

每次主观题评分成功后，重新汇总：

- `subjective_score`
- `score = objective_score + subjective_score`
- `pending_subjective_count`
- `failed_subjective_count`
- `grading_status`

不得再使用“答对题数折算总分”的旧逻辑作为最终成绩。

- [ ] **Step 3：兼容 `correctQuestions` 字段**

保留 `correctQuestions` 作为兼容展示字段，但其含义改为“完全正确题数”：

- 客观题判对计入；
- 主观题仅满分时计入。

最终成绩展示以 `score` 为准，不再以 `correctQuestions` 代表评分主结果。

---

## Task 7：前端提交态、轮询刷新与详情展示

**文件：**

- 修改：`PracticeSessionView.vue`
- 修改：`PracticeRecordsView.vue`
- 修改：`PracticeRecordDetailView.vue`
- 修改：`ExamSessionView.vue`
- 修改：`ExamRecordListView.vue`
- 修改：`ExamRecordDetailView.vue`

- [ ] **Step 1：练习页展示“评分中 / 正确 / 错误 / 失败”**

练习提交成功后：

- 客观题即时展示结果；
- 简答题先显示 `评分中`；
- 评分完成后再显示 `回答正确` 或 `回答有误`；
- 评分失败时显示 `评分失败`。

不展示练习简答题分数。

- [ ] **Step 2：考试页区分即时结果和最终结果**

考试提交成功后：

- 先显示客观题即时得分；
- 若存在待评分简答题，明确显示“主观题评分中”；
- `finalScore` 在主观题未完成前不应展示为最终成绩。

- [ ] **Step 3：实现轮询刷新**

若记录存在待评分简答题，前端每 `3-5` 秒拉一次详情接口。  
满足以下条件就停止轮询：

- 全部完成；
- 有失败；
- 页面离开。

- [ ] **Step 4：详情页展示评分反馈**

练习和考试详情页都应展示：

- 当前评分状态；
- AI 反馈摘要；
- 失败时的错误提示（面向用户的版本需简化）。

---

## Task 8：管理端任务管理与手动重试

**文件：**

- 新增：`aishua-master/src/main/java/zysy/iflytek/aishua/modules/ai/controller/AdminAiGradingController.java`
- 新增：`aishua-master/src/main/java/zysy/iflytek/aishua/modules/ai/entity/vo/AdminAiGradingTaskVO.java`
- 新增：`aishua-web/src/modules/admin/api/aiGrading.js`
- 新增：`aishua-web/src/modules/admin/views/AdminAiGradingTaskView.vue`
- 修改：`aishua-web/src/router/index.js`
- 修改：`aishua-web/src/router/navigation.js`
- 修改：`aishua-web/src/modules/admin/views/AdminDashboardView.vue`

- [ ] **Step 1：新增后台查询接口**

至少支持：

- 按状态筛选
- 按业务类型筛选
- 按用户或题目筛选
- 查看失败原因
- 查看请求 / 响应快照

- [ ] **Step 2：新增手动重试接口**

只允许对 `FAILED` 任务发起重试。  
重试接口不直接改旧任务状态，而是创建新任务。

- [ ] **Step 3：新增管理页**

管理页至少要有：

- 任务列表
- 状态筛选
- 失败原因查看
- 快照查看
- 重试按钮

由于当前管理端没有练习记录管理页，这个页面必须同时覆盖练习和考试任务。

---

## Task 9：验证与验收

**文件：**

- 所有本次变更文件

- [ ] **Step 1：补后端测试**

至少新增这些测试：

- 练习提交后简答题任务创建测试
- Worker 状态流转测试
- 练习评分成功后统计延迟写入测试
- 练习评分失败不污染统计测试
- 考试部分得分汇总测试
- 手动重试创建新任务测试

- [ ] **Step 2：跑后端验证**

运行：

```powershell
cd aishua-master
mvn test
```

若全量测试受环境影响，至少保证新增的目标测试通过，并记录失败原因。

- [ ] **Step 3：跑前端验证**

运行：

```powershell
cd aishua-web
npm run lint
npm run build
```

- [ ] **Step 4：人工验收**

至少验证：

- 练习提交后简答题先显示 `评分中`。
- 练习评分失败时错题本和掌握度不被污染。
- 考试提交后客观题结果立即可见，最终总分在主观题完成后刷新。
- 纯画布作答的简答题可以被评分。
- 文本与画布冲突时，练习判错，考试产生扣分或低置信度结果。
- 管理员可查看失败任务并手动重试。

---

## 分阶段实施建议

### 阶段 1：后端基础设施先落地

优先完成：

- 表结构变更
- 评分任务模型
- Worker
- 多模态评分客户端

交付标准：

- 不改前端也能通过接口触发任务、完成评分和回写。

### 阶段 2：先接练习链路

优先完成：

- 练习提交后异步评分
- 练习详情和记录态展示
- 评分成功后再补写统计

交付标准：

- 练习链路能完整跑通，且失败不会污染错题和掌握度。

### 阶段 3：再接考试链路

优先完成：

- 考试客观题即时得分
- 简答题部分得分回写
- 总分重算
- 前端轮询刷新最终成绩

交付标准：

- 考试总分完全切换到“客观题 + 主观题”累加口径。

### 阶段 4：管理端与运维闭环

优先完成：

- 任务管理页
- 失败快照查看
- 手动重试

交付标准：

- 失败任务可定位、可重试、可追踪。

---

## 自检

- 方案边界：已覆盖练习与考试两条链路，且明确区分“练习只对错、考试按分值”。
- 状态流转：已覆盖 `PENDING / PROCESSING / SUCCESS / FAILED` 及手动重试。
- 运维闭环：已覆盖失败原因、请求快照、响应快照、管理端重试。
- 风险控制：已避免自动重试、避免在练习失败时污染统计、避免继续使用考试旧总分算法。
