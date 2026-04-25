# 自定义审批流设计

## 目标

在 RuoYi 管理系统中新增一套通用审批流能力，后续业务模块可以通过业务类型、业务主键和业务标题接入审批，而不是每个业务各自实现审批状态、待办、记录和权限校验。

首版采用轻量审批流，不引入 Flowable、Activiti、Camunda 等流程引擎。已确认的核心规则是：审批人支持指定用户和指定角色；同一节点采用或签，一人同意即可通过；驳回时退回上一节点继续流转。

## 范围

本次包含：

- 新增审批流程定义管理。
- 新增线性审批节点配置。
- 新增节点审批人配置，支持指定用户和指定角色。
- 新增通用审批发起能力，通过 `businessType`、`businessId`、`businessTitle` 关联外部业务。
- 新增审批实例、待办任务和审批记录。
- 支持同意、驳回两类审批动作。
- 支持非首节点驳回退回上一节点。
- 支持首节点驳回后实例进入已驳回状态。
- 支持我的申请、待我审批、审批详情和审批记录查询。
- 新增后端接口权限、菜单权限和前端页面。

本次不包含：

- BPMN 画布。
- 条件网关、并行分支、会签、加签、转办、委托、抄送、催办、超时升级。
- 动态表单设计器。
- 具体业务模块状态机改造。
- 流程引擎依赖。

## 架构

功能分为五层：

1. 前端审批页面

   在 `ruoyi-ui/src/views/approval/` 下新增流程定义、我的申请、待我审批和审批详情页面。页面沿用 RuoYi + Element UI 的表格、表单、弹窗和权限按钮风格。

2. 前端 API 封装

   在 `ruoyi-ui/src/api/approval/` 下新增流程定义、审批实例和审批任务 API。页面不直接拼接请求路径，统一通过 API 文件访问后端。

3. 后端控制器

   在 `ruoyi-admin` 新增审批相关 Controller，接口按流程定义、审批实例、审批任务拆分。所有接口沿用 `@PreAuthorize("@ss.hasPermi(...)")` 权限校验，审批动作再额外校验任务归属。

4. 业务服务与流转服务

   在 `ruoyi-system` 新增审批 domain、mapper、service。Service 负责流程定义校验、审批发起、任务生成、同意流转、驳回回退、记录写入和查询隔离。

5. 数据持久化

   使用 MyBatis XML 映射新增审批表。审批模块只保存业务引用，不直接操作外部业务表。

## 数据模型

新增 `approval_process`：

- `process_id`：主键。
- `process_name`：流程名称。
- `business_type`：业务类型，用于业务接入时匹配流程。
- `status`：流程状态，停用或启用。
- `create_by`、`create_time`、`update_by`、`update_time`、`remark`：沿用 RuoYi 审计字段。

新增 `approval_node`：

- `node_id`：主键。
- `process_id`：所属流程。
- `node_name`：节点名称。
- `node_order`：节点顺序。
- `status`：节点状态。

新增 `approval_node_approver`：

- `approver_id`：主键。
- `node_id`：所属节点。
- `approver_type`：`USER` 或 `ROLE`。
- `user_id`：指定用户审批人。
- `role_id`：指定角色审批人。

新增 `approval_instance`：

- `instance_id`：主键。
- `process_id`：使用的流程。
- `business_type`：业务类型。
- `business_id`：业务主键。
- `business_title`：业务标题。
- `starter_user_id`：发起人。
- `current_node_id`：当前节点。
- `status`：`pending`、`approved`、`rejected` 等实例状态。
- `create_by`、`create_time`、`update_by`、`update_time`、`remark`：审计字段。

新增 `approval_task`：

- `task_id`：主键。
- `instance_id`：审批实例。
- `node_id`：审批节点。
- `approver_user_id`：具体审批人。
- `status`：`pending`、`approved`、`rejected`、`closed`。
- `handled_time`：处理时间。

新增 `approval_record`：

- `record_id`：主键。
- `instance_id`：审批实例。
- `task_id`：审批任务。
- `from_node_id`：操作前节点。
- `to_node_id`：操作后目标节点。
- `action`：`approve` 或 `reject`。
- `comment`：审批意见。
- `operator_user_id`：操作人。
- `create_time`：操作时间。

## 核心流程

### 发起审批

1. 业务模块提交 `businessType`、`businessId`、`businessTitle`。
2. 审批服务查找该业务类型启用的流程。
3. 校验流程至少有一个节点，且每个节点都有审批人。
4. 创建 `approval_instance`，状态为 `pending`。
5. 找到首个节点。
6. 根据首节点审批人规则生成 `approval_task`。
7. 返回审批实例信息。

指定用户审批人直接生成用户任务。指定角色审批人在发起时按当前角色成员展开成具体用户任务，保证历史审批责任稳定。

### 同意审批

1. 用户提交任务 ID 和审批意见。
2. 系统校验任务属于当前用户，且状态为 `pending`。
3. 写入同意审批记录。
4. 将当前任务标记为 `approved`。
5. 将同节点其他未处理任务标记为 `closed`。
6. 如果存在下一节点，实例流转到下一节点并生成下一节点任务。
7. 如果不存在下一节点，实例状态更新为 `approved`。

### 驳回审批

1. 用户提交任务 ID 和审批意见。
2. 系统校验任务属于当前用户，且状态为 `pending`。
3. 写入驳回审批记录。
4. 将当前任务标记为 `rejected`。
5. 将同节点其他未处理任务标记为 `closed`。
6. 如果存在上一节点，实例当前节点回退到上一节点，并重新生成上一节点任务。
7. 如果当前节点是首节点，实例状态更新为 `rejected`。

## 后端接口

建议基础路径：

- `/approval/process`：流程定义和节点配置。
- `/approval/instance`：审批发起、我的申请、审批详情。
- `/approval/task`：待我审批、同意、驳回。

建议权限标识：

- `approval:process:list`
- `approval:process:query`
- `approval:process:add`
- `approval:process:edit`
- `approval:process:remove`
- `approval:process:enable`
- `approval:instance:list`
- `approval:instance:query`
- `approval:instance:start`
- `approval:task:list`
- `approval:task:approve`
- `approval:task:reject`

## 前端交互

流程定义页面：

- 列表查询流程名称、业务类型、状态。
- 新增和编辑流程基础信息。
- 配置节点顺序、节点名称、审批人类型和审批人。
- 启用前提示必须完成节点和审批人配置。

我的申请页面：

- 展示当前用户发起的审批实例。
- 支持按业务标题、业务类型、状态查询。
- 可打开审批详情。

待我审批页面：

- 展示当前用户待处理任务。
- 支持查看详情。
- 支持同意和驳回，必须填写或允许填写审批意见。

审批详情：

- 展示业务标题、业务类型、实例状态、当前节点。
- 展示审批记录时间线。
- 展示当前待办信息。

## 权限与隔离

- 流程定义管理只对有管理权限的用户开放。
- 待我审批只返回当前登录用户的 `pending` 任务。
- 审批动作必须同时满足接口权限和任务归属。
- 我的申请只返回当前登录用户发起的实例。
- 普通用户不能查看他人申请详情。

## 验证标准

后端测试：

- 流程无节点或节点无审批人时不能启用。
- 发起审批能按业务类型匹配启用流程。
- 指定用户审批人能生成对应用户待办。
- 指定角色审批人能按发起时角色成员生成待办。
- 或签节点任一候选人同意后，节点通过且同节点其他任务关闭。
- 最后节点同意后，实例状态变为已通过。
- 非首节点驳回后，实例退回上一节点并生成上一节点待办。
- 首节点驳回后，实例状态变为已驳回。
- 用户不能处理不属于自己的任务。
- 用户不能查看他人发起的审批详情。
- 同意和驳回都会写入审批记录。

集成验证：

- 后端 Maven 编译或测试通过。
- 前端构建通过。
- 菜单权限初始化后能看到审批管理入口。
- 管理员能配置流程、节点、指定用户和指定角色审批人。
- 普通用户能发起审批并在我的申请中看到实例。
- 审批人能在待我审批中看到待办并处理。
- 驳回回退上一节点后，上一节点审批人能重新看到待办。

## 待确认

- 首个具体接入审批的业务模块暂未确定，首版先实现通用能力。
- 启用后的流程定义是否允许编辑节点：建议首版允许停用后编辑，已产生实例的节点不允许删除。
- 驳回后的重新提交由具体业务模块负责，审批模块只提供实例状态和审批记录。
