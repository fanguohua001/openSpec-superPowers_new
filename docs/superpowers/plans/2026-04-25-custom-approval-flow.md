# 自定义审批流实施计划

## 成功标准

- OpenSpec 变更 `add-custom-approval-flow` 保持有效。
- 新增通用审批流能力，后续业务可通过 `businessType`、`businessId`、`businessTitle` 接入。
- 流程定义支持线性节点，节点审批人支持指定用户和指定角色。
- 节点采用或签，一名候选审批人同意即可进入下一节点。
- 非首节点驳回退回上一节点继续流转，首节点驳回后实例为已驳回。
- 我的申请、待我审批、审批详情、审批记录满足当前用户隔离和任务归属校验。
- 后端相关测试先失败后通过。
- Maven 后端构建或测试通过。
- 前端构建通过。

## 阶段 1：后端测试骨架

1. 梳理 `ruoyi-system` 现有测试风格。
   verify: 能确定 JUnit 版本、Mockito 用法、测试命令。
2. 新增审批核心 service 测试类，先覆盖流程启用校验。
   verify: 运行单测，确认因生产类或方法缺失而失败。
3. 逐步新增发起审批、指定用户、指定角色、或签、驳回、权限隔离、审批记录测试。
   verify: 每个测试在实现前都能看到预期失败。

## 阶段 2：后端核心模型与流转

1. 新增审批 domain、mapper 接口和 service 接口。
   verify: 测试从编译失败推进到业务断言失败。
2. 实现流程定义完整性校验。
   verify: 流程启用校验测试通过。
3. 实现审批发起和首节点任务生成。
   verify: 发起审批、指定用户、指定角色测试通过。
4. 实现同意流转和或签关闭其他待办。
   verify: 或签、最后节点通过测试通过。
5. 实现驳回回退上一节点和首节点驳回。
   verify: 驳回相关测试通过。
6. 实现我的申请、待我审批、详情、记录查询和任务归属校验。
   verify: 隔离和记录测试通过。

## 阶段 3：数据库与菜单

1. 在 `sql/ry_20250522.sql` 新增审批相关表。
   verify: 表字段覆盖设计文档中的流程、节点、审批人、实例、任务、记录。
2. 新增必要索引。
   verify: 索引覆盖业务引用、发起人、审批人、状态查询。
3. 新增审批管理菜单和按钮权限。
   verify: 权限标识与后端 `@PreAuthorize`、前端 `v-hasPermi` 一致。

## 阶段 4：后端接口

1. 新增流程定义 Controller。
   verify: 接口路径、权限、参数类型符合 RuoYi 风格。
2. 新增审批实例 Controller。
   verify: 支持发起审批、我的申请、详情、按业务引用查询。
3. 新增审批任务 Controller。
   verify: 支持待我审批、同意、驳回。

## 阶段 5：前端

1. 新增 `src/api/approval` API 文件。
   verify: API 路径与后端一致。
2. 新增流程定义页面。
   verify: 支持流程基础信息、节点、审批人配置和启停。
3. 新增我的申请页面。
   verify: 支持查询和查看详情。
4. 新增待我审批页面。
   verify: 支持查询、同意、驳回。
5. 新增审批详情视图。
   verify: 展示实例信息、当前状态、当前节点和审批记录。

## 阶段 6：最终验证

1. 运行后端审批相关测试。
   verify: 审批相关测试通过。
2. 运行 Maven 后端构建或测试。
   verify: 命令 exit code 为 0。
3. 运行前端构建。
   verify: 命令 exit code 为 0。
4. 运行 OpenSpec 校验。
   verify: `openspec validate add-custom-approval-flow --no-color` 通过。
5. 同步 `openspec/changes/add-custom-approval-flow/tasks.md` 任务勾选状态。
   verify: 已完成项全部勾选，未完成项保留未勾选并说明原因。
