# 团队偏好

本文件用于记录持久的协作偏好、沟通边界和团队特定工作约定。

## 条目模板

```md
### 偏好：<简短标题>
- id: preference-YYYY-MM-DD-<slug>
- type: team_preference
- status: active
- confidence: verified
- last_updated: YYYY-MM-DD
- source:
- owner:
- review_after:

偏好：

设置原因：

应用方式：
```

## 记录说明

- 本文件只记录持久偏好，不记录临时会话请求。
- 如果偏好发生变化，更新既有条目或标记为 `superseded`。

### 偏好：默认使用简体中文
- id: preference-2026-04-28-default-zh-cn
- type: team_preference
- status: active
- confidence: verified
- last_updated: 2026-04-28
- source: `AGENTS.md`
- owner: 项目团队
- review_after: 仓库语言规则变化时复审

偏好：

普通回复、任务清单、计划、评审意见、总结、生成的自然语言内容、注释和解释默认使用简体中文，除非用户明确要求英文或其他语言。

设置原因：

仓库协作规则优先要求自然语言内容使用中文。

应用方式：

代码、命令、路径、配置键、接口字段、日志和原始报错保持原样。

### 偏好：技能和工作流需要显式选择启用
- id: preference-2026-04-28-explicit-skill-opt-in
- type: team_preference
- status: active
- confidence: verified
- last_updated: 2026-04-28
- source: `AGENTS.md`, `.cursor/rules/superpowers-memory.mdc`
- owner: 项目团队
- review_after: 协作规则变化时复审

偏好：

只有当用户明确点名、明确要求，或仓库规则明确要求时，才调用技能或工作流。

设置原因：

仓库希望减少意外的 AI workflow 激活，让工具使用更有意识。

应用方式：

读取 `.superpowers-memory/` 可以作为上下文恢复；激活 Superpowers 工作流仍然是独立的显式选择启用。

### 偏好：Markdown 自然语言默认中文
- id: preference-2026-04-28-markdown-zh-cn
- type: team_preference
- status: active
- confidence: verified
- last_updated: 2026-04-28
- source: `AGENTS.md`
- owner: 项目团队
- review_after: 仓库文档语言规则变化时复审

偏好：

生成或更新 Markdown 文件时，自然语言标题、正文、列表项、说明、模板文本和注释默认使用简体中文。

设置原因：

用户明确要求把生成的 Markdown 文件内容及标题转换为中文，并把该规则写入 `AGENTS.md`。

应用方式：

Markdown 中的代码块、命令、路径、配置键、接口字段、日志、报错原文和必要英文专有名词保持原样，除非用户明确要求翻译或改写。
