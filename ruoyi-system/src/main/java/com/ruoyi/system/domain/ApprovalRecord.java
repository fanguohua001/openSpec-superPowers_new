package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class ApprovalRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long recordId;

    private Long instanceId;

    private Long taskId;

    private Long fromNodeId;

    private Long toNodeId;

    private String action;

    private String comment;

    private Long operatorUserId;

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(Long instanceId)
    {
        this.instanceId = instanceId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getFromNodeId()
    {
        return fromNodeId;
    }

    public void setFromNodeId(Long fromNodeId)
    {
        this.fromNodeId = fromNodeId;
    }

    public Long getToNodeId()
    {
        return toNodeId;
    }

    public void setToNodeId(Long toNodeId)
    {
        this.toNodeId = toNodeId;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Long getOperatorUserId()
    {
        return operatorUserId;
    }

    public void setOperatorUserId(Long operatorUserId)
    {
        this.operatorUserId = operatorUserId;
    }
}
