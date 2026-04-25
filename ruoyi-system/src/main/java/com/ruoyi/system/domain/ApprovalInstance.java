package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class ApprovalInstance extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long instanceId;

    private Long processId;

    private String businessType;

    private String businessId;

    private String businessTitle;

    private Long starterUserId;

    private Long currentNodeId;

    private String status;

    public Long getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(Long instanceId)
    {
        this.instanceId = instanceId;
    }

    public Long getProcessId()
    {
        return processId;
    }

    public void setProcessId(Long processId)
    {
        this.processId = processId;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public String getBusinessId()
    {
        return businessId;
    }

    public void setBusinessId(String businessId)
    {
        this.businessId = businessId;
    }

    public String getBusinessTitle()
    {
        return businessTitle;
    }

    public void setBusinessTitle(String businessTitle)
    {
        this.businessTitle = businessTitle;
    }

    public Long getStarterUserId()
    {
        return starterUserId;
    }

    public void setStarterUserId(Long starterUserId)
    {
        this.starterUserId = starterUserId;
    }

    public Long getCurrentNodeId()
    {
        return currentNodeId;
    }

    public void setCurrentNodeId(Long currentNodeId)
    {
        this.currentNodeId = currentNodeId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
