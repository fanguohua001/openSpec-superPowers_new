package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class ApprovalNodeApprover extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long approverId;

    private Long nodeId;

    private String approverType;

    private Long userId;

    private Long roleId;

    public Long getApproverId()
    {
        return approverId;
    }

    public void setApproverId(Long approverId)
    {
        this.approverId = approverId;
    }

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    public String getApproverType()
    {
        return approverType;
    }

    public void setApproverType(String approverType)
    {
        this.approverType = approverType;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(Long roleId)
    {
        this.roleId = roleId;
    }
}
