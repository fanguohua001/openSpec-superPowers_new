package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class ApprovalProcess extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long processId;

    private String processName;

    private String businessType;

    private String status;

    public Long getProcessId()
    {
        return processId;
    }

    public void setProcessId(Long processId)
    {
        this.processId = processId;
    }

    public String getProcessName()
    {
        return processName;
    }

    public void setProcessName(String processName)
    {
        this.processName = processName;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
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
