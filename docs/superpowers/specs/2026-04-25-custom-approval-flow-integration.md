# 自定义审批流接入说明

## 发起审批

业务模块需要准备三个字段：

- `businessType`：业务类型，例如 `leave`、`contract`。
- `businessId`：业务主键，建议使用业务表主键字符串。
- `businessTitle`：业务标题，用于审批列表展示。

后端可调用 `IApprovalService.startApproval(request, starterUserId, username)`，也可以通过接口发起：

```text
POST /approval/instance/start
```

请求示例：

```json
{
  "businessType": "leave",
  "businessId": "1001",
  "businessTitle": "张三的请假申请"
}
```

## 查询审批状态

业务模块可按业务引用查询审批实例：

```text
GET /approval/instance/business?businessType=leave&businessId=1001
```

后端可调用：

```java
approvalService.selectInstanceByBusiness("leave", "1001");
```

## 业务状态处理

审批模块只维护审批实例、任务和记录，不直接修改业务表。业务模块应根据审批实例状态自行更新业务状态：

- `pending`：审批中。
- `approved`：审批通过。
- `rejected`：首节点驳回后退回发起人。

非首节点驳回会回到上一节点继续流转，审批实例仍保持 `pending`。
