package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

public class ApprovalTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long taskId;

    private Long instanceId;

    private Long nodeId;

    private Long approverUserId;

    private String status;

    private Date handledTime;

    private String processName;

    private String businessType;

    private String businessId;

    private String businessTitle;

    private String nodeName;

    private String starterUserName;

    private String approverUserName;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public Long getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(Long instanceId)
    {
        this.instanceId = instanceId;
    }

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getApproverUserId()
    {
        return approverUserId;
    }

    public void setApproverUserId(Long approverUserId)
    {
        this.approverUserId = approverUserId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getHandledTime()
    {
        return handledTime;
    }

    public void setHandledTime(Date handledTime)
    {
        this.handledTime = handledTime;
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

    public String getNodeName()
    {
        return nodeName;
    }

    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    public String getStarterUserName()
    {
        return starterUserName;
    }

    public void setStarterUserName(String starterUserName)
    {
        this.starterUserName = starterUserName;
    }

    public String getApproverUserName()
    {
        return approverUserName;
    }

    public void setApproverUserName(String approverUserName)
    {
        this.approverUserName = approverUserName;
    }
}
