# 决策记录

本文件用于记录应跨会话保留的重要项目决策或工作流决策。

## 条目模板

```md
### 决策：<简短标题>
- id: decision-YYYY-MM-DD-<slug>
- type: decision
- status: active
- confidence: verified
- last_updated: YYYY-MM-DD
- source:
- owner:
- review_after:

原因：

考虑过的替代方案：

影响：
```

## 记录说明

- 只记录未来会话仍然需要知道的决策。
- 过时决策应标记为 `status: superseded`，不要随意删除历史。
- 尽量引用代码、文档、测试或会话记录作为依据。

### 决策：使用轻量级自定义审批流
- id: decision-2026-04-28-lightweight-approval-flow
- type: decision
- status: active
- confidence: verified
- last_updated: 2026-04-28
- source: `openspec/changes/archive/2026-04-26-add-custom-approval-flow/proposal.md`, `openspec/specs/custom-approval-flow/spec.md`
- owner: 项目团队
- review_after: 当审批需求超出线性或签流程时复审

原因：

项目需要可复用审批能力，避免后续业务模块重复实现状态、权限、记录和流转规则。已接受的变更选择了小型项目原生审批模块，而不是引入 Flowable、Activiti、Camunda、BPMN 画布、网关、委托、超时提醒等工作流引擎能力。

考虑过的替代方案：

外部工作流引擎和更完整的 BPMN 建模能力已明确不纳入首版范围。

影响：

后续审批相关工作应优先扩展现有 RuoYi 风格的 Controller/Service/Mapper/Vue 实现，除非需求明显超过当前轻量级设计。

### 决策：技能和工作流保持显式选择启用
- id: decision-2026-04-28-explicit-skill-opt-in
- type: decision
- status: active
- confidence: verified
- last_updated: 2026-04-28
- source: `AGENTS.md`, `.cursor/rules/superpowers-memory.mdc`
- owner: 项目团队
- review_after: 仓库协作规则变化时复审

原因：

仓库规则说明，不能只因为任务类型匹配就调用技能或工作流；必须由用户或项目规则显式要求。

考虑过的替代方案：

自动启用匹配工作流会与当前本地协作规则冲突。

影响：

后续助手可以在记忆文件存在时读取上下文，但不能把读取记忆视为激活 Superpowers 工作流的许可。

### 决策：扫雷首版保持纯前端休闲功能
- id: decision-2026-04-28-minesweeper-frontend-only
- type: decision
- status: active
- confidence: verified
- last_updated: 2026-04-28
- source: `docs/superpowers/specs/2026-04-28-minesweeper-game-design.md`, `openspec/specs/minesweeper-game/spec.md`, `ruoyi-ui/src/views/game/minesweeper/`
- owner: 项目团队
- review_after: 当需要排行榜、历史战绩或跨用户能力时复审

原因：

用户确认的首版方案是“轻量前端游戏 + 菜单接入”。扫雷本身不需要后端业务状态，纯前端实现能保持改动范围小，并通过纯 JavaScript 核心测试覆盖主要规则。

考虑过的替代方案：

后端排行榜、历史记录和个人最佳成绩持久化可以作为后续扩展，但首版不引入数据库表、Controller、Service 或前端 API。

影响：

后续维护扫雷时，应优先保持 `minesweeper.js` 作为规则核心，页面只负责渲染和交互。若新增排行榜等持久化能力，应单独提出 OpenSpec 变更。
