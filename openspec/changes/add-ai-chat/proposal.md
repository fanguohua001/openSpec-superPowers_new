## Why

当前项目是标准 RuoYi 管理系统，尚未提供 AI 助手能力。接入可持久化的 AI 聊天，可以让后台用户在系统内完成连续问答，并为后续知识库、业务辅助生成、模型适配扩展打下基础。

本次选择先做会话持久化和流式响应，是为了同时满足可用体验和可追溯历史；模型服务协议暂不确定，因此需要保留适配边界，避免后续供应商变化导致业务代码大面积改动。

## What Changes

- 新增“AI 聊天”后台菜单，入口位于现有“系统工具”下。
- 新增按当前登录用户隔离的聊天会话列表、消息历史查询和会话删除能力。
- 新增发送消息并流式接收助手回复的接口。
- 新增会话表和消息表，持久化用户消息、助手消息、生成状态和失败原因。
- 新增从 `sys_config` 读取模型服务配置的能力，包括 `baseUrl`、`apiKey`、`model`、`temperature`、`timeoutSeconds`。
- 新增默认 OpenAI Chat Completions 兼容的流式模型适配器。
- 新增前端聊天页面，支持会话切换、消息展示、流式增量显示、发送中状态和基础错误展示。
- 不引入破坏性变更；现有系统菜单、权限、用户、角色、配置管理能力保持兼容。

## Capabilities

### New Capabilities

- `ai-chat`: 定义 AI 聊天能力，包括会话持久化、用户隔离、消息发送、流式回复、模型配置读取和失败状态处理。

### Modified Capabilities

无。

## Impact

- 后端模块：
  - `ruoyi-admin`：新增 AI 聊天 Controller。
  - `ruoyi-system`：新增 AI 聊天 domain、mapper、service、模型适配边界和默认适配器。
  - `ruoyi-common`：仅在确有必要时补充通用常量或异常，默认不改。
- 前端模块：
  - `ruoyi-ui/src/api/tool/aiChat.js`
  - `ruoyi-ui/src/views/tool/aiChat/index.vue`
- 数据库：
  - 新增 `ai_chat_session`。
  - 新增 `ai_chat_message`。
  - 新增 `sys_menu` 初始化数据。
  - 新增 `sys_config` 初始化数据。
- 外部系统：
  - 依赖一个 OpenAI Chat Completions 兼容或后续可适配的第三方模型服务。
- 安全与权限：
  - 新增 `tool:aiChat:list`、`tool:aiChat:send`、`tool:aiChat:remove` 权限。
  - `apiKey` 作为敏感配置处理，不在 AI 聊天页面展示明文。
