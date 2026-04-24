# AI 聊天规格

## Purpose
定义 RuoYi 管理台内 AI 聊天能力的菜单入口、会话与消息持久化、模型配置读取、流式回复、失败状态处理和前端交互要求。

## Requirements
### Requirement: AI 聊天菜单入口
系统 SHALL 在现有“系统工具”菜单下提供 AI 聊天入口，并通过 RuoYi 菜单权限控制访问。

#### Scenario: 管理员看到 AI 聊天菜单
- **WHEN** 管理员拥有 `tool:aiChat:list` 权限并登录系统
- **THEN** 系统必须在“系统工具”下展示 AI 聊天菜单

#### Scenario: 无权限用户不能访问 AI 聊天页面
- **WHEN** 用户没有 `tool:aiChat:list` 权限
- **THEN** 系统必须不向该用户提供 AI 聊天页面访问能力

### Requirement: 用户会话持久化
系统 SHALL 为每个登录用户保存独立的 AI 聊天会话，并禁止用户读取或删除其他用户的会话。

#### Scenario: 查询当前用户会话
- **WHEN** 用户请求 AI 聊天会话列表
- **THEN** 系统必须只返回该用户自己的会话

#### Scenario: 删除当前用户会话
- **WHEN** 用户删除自己拥有的会话
- **THEN** 系统必须将该会话从用户可见列表中移除

#### Scenario: 拒绝跨用户访问
- **WHEN** 用户请求不属于自己的会话消息
- **THEN** 系统必须拒绝访问或返回空结果

### Requirement: 消息历史持久化
系统 SHALL 持久化用户消息、助手消息、角色、内容、状态和失败原因。

#### Scenario: 保存用户消息
- **WHEN** 用户发送一条聊天消息
- **THEN** 系统必须在对应会话中保存一条 `user` 消息

#### Scenario: 保存助手消息
- **WHEN** 模型服务正常完成回复
- **THEN** 系统必须保存一条 `assistant` 消息，并将状态标记为正常

#### Scenario: 页面刷新后恢复历史
- **WHEN** 用户刷新 AI 聊天页面并重新打开同一会话
- **THEN** 系统必须展示该会话已保存的历史消息

### Requirement: 流式发送和回复
系统 SHALL 支持用户发送消息后以 `text/event-stream` 流式返回助手回复。

#### Scenario: 正常流式回复
- **WHEN** 用户发送消息且模型服务返回流式增量
- **THEN** 系统必须向前端持续发送 `delta` 事件
- **THEN** 系统必须在完成时发送 `done` 事件

#### Scenario: 自动创建会话并发送
- **WHEN** 用户在没有指定会话的情况下发送消息
- **THEN** 系统必须自动创建会话
- **THEN** 系统必须通过 `session` 事件返回新会话信息

#### Scenario: 返回消息标识
- **WHEN** 系统开始处理一次发送请求
- **THEN** 系统必须通过 `message` 事件返回用户消息 ID 和助手消息 ID

### Requirement: 模型配置读取
系统 SHALL 从 `sys_config` 读取 AI 聊天模型服务配置。

#### Scenario: 配置完整时调用模型
- **WHEN** `ai.chat.baseUrl`、`ai.chat.apiKey` 和 `ai.chat.model` 均已配置
- **THEN** 系统必须使用这些配置调用模型服务

#### Scenario: 缺少必需配置时拒绝发送
- **WHEN** 必需模型配置缺失
- **THEN** 系统必须拒绝发送请求并返回清晰错误

### Requirement: 默认 OpenAI 兼容适配器
系统 SHALL 提供默认模型适配器，支持 OpenAI Chat Completions 兼容的流式协议，并在 Chat Completions 端点不可用时兼容 OpenAI Responses 流式协议。

#### Scenario: 发送 OpenAI 兼容请求
- **WHEN** 系统调用默认模型适配器
- **THEN** 适配器必须发送包含 `model`、`messages` 和 `stream: true` 的聊天请求

#### Scenario: 兼容包含 `/v1` 的基础地址
- **WHEN** `ai.chat.baseUrl` 已包含 `/v1`
- **THEN** 适配器不得重复拼接 `/v1`

#### Scenario: 回退到 Responses 流式协议
- **WHEN** Chat Completions 端点不可用且 Responses 端点可用
- **THEN** 适配器必须使用包含 `model`、`input` 和 `stream: true` 的 Responses 请求继续调用模型服务

#### Scenario: 解析流式增量
- **WHEN** 模型服务返回 SSE 数据块
- **THEN** 适配器必须解析增量文本并回调给业务服务

#### Scenario: 识别完成事件
- **WHEN** 模型服务返回完成标记
- **THEN** 适配器必须通知业务服务流式回复已完成

### Requirement: 失败状态处理
系统 SHALL 在模型服务、网络、解析或流式处理失败时保存失败状态，并让前端展示错误。

#### Scenario: 模型调用失败
- **WHEN** 模型服务调用失败
- **THEN** 系统必须保存用户消息
- **THEN** 系统必须保存一条失败状态的助手消息并记录 `error_message`
- **THEN** 系统必须向前端发送 `error` 事件

#### Scenario: 流式过程中失败
- **WHEN** 已开始流式回复但中途发生错误
- **THEN** 系统必须持久化助手消息的失败状态
- **THEN** 系统必须让前端在当前助手消息位置展示失败信息

### Requirement: 前端聊天交互
系统 SHALL 提供符合 RuoYi 管理台风格的 AI 聊天页面，支持会话切换、消息展示、发送、停止和错误展示。

#### Scenario: 发送中禁止重复发送
- **WHEN** 当前会话正在等待流式回复
- **THEN** 前端必须禁止重复发送新的消息

#### Scenario: 增量显示助手回复
- **WHEN** 前端收到 `delta` 事件
- **THEN** 前端必须将增量文本追加到当前助手消息中

#### Scenario: 停止当前回复
- **WHEN** 用户点击停止按钮
- **THEN** 前端必须关闭当前流式请求连接

#### Scenario: 显示失败消息
- **WHEN** 前端收到 `error` 事件
- **THEN** 前端必须在当前助手消息位置展示失败状态
