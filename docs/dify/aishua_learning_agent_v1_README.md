# aishua_learning_agent_v1 Dify 工作流

本目录用于先搭起 Dify 学习助手的第一版链路：

- 后端/Run 传入学习上下文
- Dify 解析输入并返回稳定 `data.outputs`
- 没有搜索结果时不编造 URL，返回学习建议和 `data_quality.sufficient=false`

## 文件

- `aishua_learning_agent_v1_bootstrap.dsl.yml`：可导入 Dify 的 Workflow DSL。
- `aishua_learning_agent_v1_acceptance.json`：Dify 页面 Run 测试输入。
- `aishua_learning_agent_v1_api_body.json`：`POST /v1/workflows/run` 的 blocking 测试体。

## 导入步骤

1. 打开 `http://localhost/apps` 并登录 Dify。
2. 进入 `Studio`。
3. 选择 `Import DSL` / `Create from DSL`。
4. 上传 `docs/dify/aishua_learning_agent_v1_bootstrap.dsl.yml`。
5. 导入后确认应用名称为 `aishua_learning_agent_v1`，类型为 `Workflow`。
6. 在工作流 Run 面板粘贴 `aishua_learning_agent_v1_acceptance.json` 进行测试。

预期输出：

- `answer` 非空。
- `resources` 是数组；bootstrap 版本没有搜索分支，因此为空数组。
- `focus_tags` 包含 `椭圆定义`。
- `data_quality.sufficient=false`。
- `trace.need_web_search=true`。

## API 调用

导入并发布后，在 Dify 应用的 API Access 中创建应用 API Key，然后设置后端环境变量：

```yaml
dify:
  learning-agent:
    base-url: ${DIFY_BASE_URL:http://localhost}
    api-key: ${DIFY_LEARNING_AGENT_API_KEY:}
    workflow-run-path: /v1/workflows/run
    response-mode: blocking
```

blocking 请求体见 `aishua_learning_agent_v1_api_body.json`。Dify Workflow API 的核心路径是：

```text
POST /v1/workflows/run
```

返回结果位于：

```text
data.outputs.answer
data.outputs.resources
data.outputs.data_quality
```

## 后续升级为完整智能体

bootstrap DSL 先保证后端解析稳定。要升级为完整智能体，在 Dify 画布里按下面结构替换 `normalize_and_respond` 节点：

1. `normalize_inputs` Code 节点：解析 `learning_context_json`、`conversation_context_json`、`local_question_candidates_json`、`resource_policy_json`。
2. `intent_classifier` Question Classifier：
   - `concept_explain`
   - `solution_strategy`
   - `resource_recommendation`
   - `practice_recommendation`
   - `mixed`
   - `off_topic`
3. `learning_planner` LLM：输出 `intent`、`target_tags`、`student_level`、`need_web_search`、`search_queries`。
4. `IF/ELSE`：`need_web_search=true` 时进入搜索分支。
5. 搜索分支：优先接 Google Search 插件；不可用时用 HTTP Request 调搜索 API。
6. `merge_search_results` Code：URL 去重，过滤广告、下载站、网盘，最多保留 8 条。
7. `resource_ranker_and_answer` LLM：基于搜索结果和本地候选题生成最终 JSON。
8. `Output`：输出 `answer`、`focus_tags`、`resources`、`local_practice`、`next_actions`、`data_quality`、`trace`。

`learning_planner` Prompt：

```text
你是 AI刷题系统的学习路径规划器。只输出合法 JSON。
根据学生问题、掌握度、错题和最近练习记录判断意图、涉及考点、是否需要联网搜索，以及搜索关键词。
target_tags 优先来自 learning_context_json 中已有 tagName。
如果用户请求资料、视频、网页、题目链接，need_web_search 必须为 true。
```

`resource_ranker_and_answer` Prompt：

```text
你是中学学习资源推荐助手。你必须结合学生掌握情况推荐资源。
只使用输入中提供的搜索结果、网页摘要和本地题目候选，不得编造 URL。
输出必须是合法 JSON，字段包括 answer、focus_tags、resources、local_practice、next_actions、data_quality。
```

## 输出结构约定

```json
{
  "answer": "string",
  "focus_tags": ["string"],
  "resources": [
    {
      "title": "string",
      "url": "string",
      "type": "video|article|exercise|course|other",
      "reason": "string"
    }
  ],
  "local_practice": [
    {
      "question_id": "string|number",
      "title": "string",
      "difficulty": "string",
      "reason": "string"
    }
  ],
  "next_actions": ["string"],
  "data_quality": {
    "sufficient": false,
    "missing": ["web_search_results"],
    "bootstrap": true
  },
  "trace": {
    "workflow": "aishua_learning_agent_v1_bootstrap",
    "intent": "mixed",
    "need_web_search": true
  }
}
```

注意：不要把 Dify 应用 API Key 写入仓库；用 `DIFY_LEARNING_AGENT_API_KEY` 环境变量注入。
