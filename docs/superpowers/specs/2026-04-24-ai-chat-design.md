# AI 聊天设计

## 目标

在 RuoYi 管理系统中新增一个可持久化的 AI 聊天功能。首版入口位于现有“系统工具”菜单下，名称为“AI 聊天”。功能支持流式模型响应、按用户保存会话和消息，并从 `sys_config` 读取模型服务配置。

由于第三方模型服务协议暂不确定，系统会引入一个轻量的模型适配层。默认适配器按照 OpenAI Chat Completions 兼容的流式接口实现。

## 范围

本次包含：

- 在现有“系统工具”菜单下新增 AI 聊天入口。
- 支持按用户隔离的聊天会话和消息历史。
- 支持助手回复流式返回。
- 从 `sys_config` 读取模型服务配置。
- 提供默认 OpenAI 兼容流式适配器。
- 提供会话列表、发送消息、删除会话的基础权限控制。
- 模型服务或流式处理失败时持久化失败状态。

本次不包含：

- 多模型服务管理页面。
- 知识库、文件上传、联网搜索和检索增强生成。
- 精确按 token 裁剪上下文。
- 跨请求的服务端取消任务注册表。
- 独立的模型配置表。

## 架构

功能分为四层。

1. 前端聊天页面

   新增 `ruoyi-ui/src/views/tool/aiChat/index.vue` 和 `ruoyi-ui/src/api/tool/aiChat.js`。页面包含会话列表、消息区域、输入框、发送按钮和停止按钮。流式返回时，前端逐步追加助手消息内容。

2. 后端控制器

   在 `ruoyi-admin` 中新增 `AiChatController`。接口统一放在 `/tool/aiChat` 下，提供会话、消息、删除和流式发送能力。权限校验沿用 RuoYi 现有 `@PreAuthorize("@ss.hasPermi(...)")` 风格。

3. 业务与持久化层

   在 `ruoyi-system` 中新增 AI 聊天的 domain、mapper 和 service。Service 负责会话校验、用户隔离、消息保存、上下文加载和助手消息状态更新。

4. 模型适配层

   新增一个轻量模型适配边界供 Service 调用。首个适配器从 `sys_config` 读取模型服务配置，向 OpenAI 兼容接口发送流式聊天请求，解析 SSE 数据块，并通过回调返回增量文本。后续如果实际第三方服务协议不同，只新增新的适配器，不修改 Controller 和前端契约。

## 数据模型

新增 `ai_chat_session`：

- `session_id`：主键。
- `user_id`：会话所属用户 ID。所有查询和删除都按该字段隔离。
- `title`：会话标题。默认从第一条用户消息中截取生成。
- `model`：会话或最近一次发送使用的模型名。
- `status`：`0` 正常，`1` 删除或停用。
- `create_by`、`create_time`、`update_by`、`update_time`、`remark`：沿用 RuoYi 风格的审计字段。

新增 `ai_chat_message`：

- `message_id`：主键。
- `session_id`：所属会话。
- `user_id`：冗余保存所属用户 ID，便于过滤和清理。
- `role`：`user`、`assistant` 或 `system`。
- `content`：消息内容，使用 `longtext`。
- `status`：`0` 正常，`1` 生成中，`2` 失败。
- `error_message`：助手消息失败时保存失败原因。
- `create_time`：消息创建时间。

新增 `sys_config` 配置：

- `ai.chat.baseUrl`
- `ai.chat.apiKey`
- `ai.chat.model`
- `ai.chat.temperature`
- `ai.chat.timeoutSeconds`

`apiKey` 属于敏感信息。后端请求模型服务时读取真实值。前端不应随意展示明文；如果复用通用系统参数页面，实现时需要避免新增任何 AI 专属的明文展示逻辑。

新增 `sys_menu` 数据：

- 在现有“系统工具”菜单下新增“AI 聊天”，路径为 `tool/aiChat/index`。
- 权限标识：`tool:aiChat:list`、`tool:aiChat:send`、`tool:aiChat:remove`。

## 后端接口

统一使用 `/tool/aiChat` 作为基础路径。

- `GET /tool/aiChat/session/list`：分页查询当前用户的会话。
- `POST /tool/aiChat/session`：创建空会话。
- `GET /tool/aiChat/session/{sessionId}/messages`：查询当前用户某个会话的消息。
- `DELETE /tool/aiChat/session/{sessionIds}`：删除当前用户的会话。
- `POST /tool/aiChat/session/{sessionId}/message/stream`：在已有会话中发送用户消息，并流式返回助手回复。
- `POST /tool/aiChat/message/stream`：无会话发送消息，由后端自动创建会话并流式返回助手回复。

流式响应使用 `text/event-stream`。

服务端事件：

- `session`：自动创建会话时发送，包含 `sessionId` 和 `title`。
- `message`：包含已保存的用户消息 ID 和助手消息 ID。
- `delta`：助手回复的增量文本。
- `done`：生成完成。
- `error`：生成失败。

## 流式处理流程

1. 接收并校验用户输入。
2. 创建或校验当前用户的会话。
3. 保存用户消息。
4. 保存一条状态为 `1` 生成中的助手消息。
5. 加载该会话最近的上下文消息。
6. 调用模型适配器，并设置 `stream: true`。
7. 每收到一个模型增量，就发送一个 `delta` 事件，并把文本追加到内存缓冲区。
8. 正常完成时，更新助手消息内容，并把状态改为 `0`。
9. 如果出现模型服务、网络、解析或流式处理错误，把助手消息状态改为 `2`，写入 `error_message`，并发送 `error` 事件。

上下文策略：

- 只向模型发送当前会话的最近消息。
- 默认上下文窗口为最近 20 条消息。
- 只发送 `role` 和 `content`。
- 首版不做精确 token 裁剪。

停止行为：

- 前端停止按钮关闭当前请求连接。
- 后端检测到连接断开后，应尽量停止继续读取模型服务流。
- 如果已经生成了部分内容，实现时可以选择“保存部分内容并标记失败”，或“保存部分内容并附带清晰失败信息”。具体策略在实现计划中确定，并配套验证。

## 前端交互

页面沿用现有 RuoYi 和 Element UI 管理台风格。

- 左侧：会话列表，支持新建、选择和删除。
- 右侧：消息区域，区分展示用户消息和助手消息。
- 底部：输入框、发送按钮、停止按钮。
- 发送中：禁止重复发送。
- 流式中：收到 `delta` 事件后持续追加助手内容。
- 异常时：在当前助手消息位置展示失败状态，并保留用户消息。
- 刷新后：重新加载已持久化的会话和消息历史。

权限：

- 没有 `tool:aiChat:list` 时，不展示菜单或无法访问页面。
- 没有 `tool:aiChat:send` 时，禁止发送或由后端拒绝。
- 没有 `tool:aiChat:remove` 时，不展示删除会话能力。

## 验证标准

后端验证：

- Service 测试覆盖当前用户会话归属校验。
- Service 测试覆盖用户消息和助手消息保存。
- 配置测试覆盖缺少 `baseUrl`、`apiKey` 或 `model` 的错误。
- Adapter 测试使用可控假流覆盖流式回调行为。
- 错误测试覆盖模型调用失败和助手消息失败状态持久化。

前端与集成验证：

- 前端构建成功。
- SQL 初始化后，“系统工具”菜单下出现“AI 聊天”。
- 管理员可以打开 AI 聊天页面。
- 配置有效 `sys_config` 后，用户可以发送消息并看到流式增量。
- 刷新页面后，会话和消息历史仍然存在。
- 用户不能读取其他用户的会话。
- 模型服务失败时，用户消息落库，助手失败消息落库，页面显示错误。
