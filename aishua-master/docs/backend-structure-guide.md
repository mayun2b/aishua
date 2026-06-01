# 后端结构阅读指南

## 1. 后端目录总览

- `src/main/java/zysy/iflytek/aishua/common`：通用能力（异常、返回体、安全、上下文等）。
- `src/main/java/zysy/iflytek/aishua/config`：基础配置与配置属性。
- `src/main/java/zysy/iflytek/aishua/modules`：业务模块（practice/exam/question/subject/ai 等）。

## 2. 模块内分层约定

- `controller`：接口层，只负责入参校验、调用服务、返回结果。
- `service`：应用服务层，负责业务流程编排。
- `mapper`：数据访问层，负责数据库读写。
- `entity`：实体、DTO、VO。
- `support`：复用规则组件、计算逻辑、工具型能力。

## 3. practice 模块本次优化点

- 将“练习提交后统计写入”从 `PracticeServiceImpl` 拆出到 `PracticeSubmissionStatsUpdater`。
- `PracticeServiceImpl` 聚焦流程编排（会话、草稿、提交、查询），减少单类职责混合。
- 新增 `practice/package-info.java`，明确模块分层与职责边界。

## 4. 推荐阅读顺序（新同学）

1. `modules/practice/controller/PracticeController.java`：先看接口入口。  
2. `modules/practice/service/PracticeService.java`：看服务能力契约。  
3. `modules/practice/service/impl/PracticeServiceImpl.java`：看主流程编排。  
4. `modules/practice/support/*.java`：看判分、统计、规则等细粒度逻辑。  

## 5. 注释规范（统一中文）

- 业务注释统一中文，优先解释“为什么这样做”，而不是重复代码字面含义。
- 复杂流程前加短注释，说明关键约束（并发、一致性、回退策略）。
- 公共组件保留类级注释，说明职责与边界，避免职责漂移。
