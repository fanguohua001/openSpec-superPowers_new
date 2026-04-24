## 1. 数据库与初始化数据

- [x] 1.1 在 SQL 脚本中新增 `ai_chat_session` 表定义。
- [x] 1.2 在 SQL 脚本中新增 `ai_chat_message` 表定义。
- [x] 1.3 在 SQL 脚本中新增 `ai.chat.baseUrl`、`ai.chat.apiKey`、`ai.chat.model`、`ai.chat.temperature`、`ai.chat.timeoutSeconds` 的 `sys_config` 初始化数据。
- [x] 1.4 在 SQL 脚本中新增“AI 聊天”菜单和 `tool:aiChat:list`、`tool:aiChat:send`、`tool:aiChat:remove` 权限数据。

## 2. 后端测试

- [x] 2.1 新增会话归属校验测试，覆盖用户不能读取其他用户会话。
- [x] 2.2 新增消息持久化测试，覆盖用户消息和助手消息保存。
- [x] 2.3 新增配置缺失测试，覆盖缺少 `baseUrl`、`apiKey` 或 `model` 时拒绝发送。
- [x] 2.4 新增模型适配器测试，使用可控假流验证增量回调和完成事件。
- [x] 2.5 新增失败处理测试，覆盖模型调用失败时助手消息状态和 `error_message` 持久化。

## 3. 后端实现

- [x] 3.1 在 `ruoyi-system` 新增 AI 聊天 domain、DTO/VO 和状态常量。
- [x] 3.2 在 `ruoyi-system` 新增 AI 聊天 mapper 接口与 MyBatis XML。
- [x] 3.3 在 `ruoyi-system` 新增 AI 聊天 service，支持会话列表、创建、消息查询、删除和用户隔离。
- [x] 3.4 在 `ruoyi-system` 新增模型配置读取组件，从 `sys_config` 获取并校验 AI 配置。
- [x] 3.5 在 `ruoyi-system` 新增模型适配器接口和默认 OpenAI 兼容流式适配器。
- [x] 3.6 在 `ruoyi-system` 实现发送消息流程，包含用户消息落库、助手生成中消息落库、上下文加载、流式回调、完成更新和失败更新。
- [x] 3.7 在 `ruoyi-admin` 新增 `AiChatController`，暴露 `/tool/aiChat` 会话、消息、删除和流式发送接口。
- [x] 3.8 为后端接口添加 RuoYi 权限注解和必要操作日志。

## 4. 前端实现

- [x] 4.1 新增 `ruoyi-ui/src/api/tool/aiChat.js`，封装会话列表、创建、消息查询、删除和流式发送请求。
- [x] 4.2 新增 `ruoyi-ui/src/views/tool/aiChat/index.vue`，实现会话列表、消息区域、输入区、发送按钮和停止按钮。
- [x] 4.3 实现前端 SSE 读取与 `session`、`message`、`delta`、`done`、`error` 事件处理。
- [x] 4.4 实现发送中状态、重复发送保护、停止当前请求和失败消息展示。
- [x] 4.5 确认菜单路径 `tool/aiChat/index` 能正确加载前端页面。

## 5. 验证

- [x] 5.1 运行后端相关测试，确认新增测试先失败后通过。
- [x] 5.2 运行 Maven 编译或测试命令，确认后端构建通过。
- [x] 5.3 运行前端构建命令，确认前端构建通过。
- [x] 5.4 使用有效模型配置手工验证管理员能打开 AI 聊天并看到流式回复。
- [x] 5.5 手工验证刷新页面后会话和消息历史仍存在。
- [x] 5.6 手工验证模型服务失败时用户消息落库、助手失败消息落库且页面显示错误。
- [x] 5.7 运行 `openspec validate add-ai-chat --no-color`，确认 OpenSpec 变更有效。
