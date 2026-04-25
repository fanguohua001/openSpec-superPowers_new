package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.List;

public class ApprovalNode extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long nodeId;

    private Long processId;

    private String nodeName;

    private Integer nodeOrder;

    private String status;

    private List<ApprovalNodeApprover> approvers;

    public Long getNodeId()
    {
        return nodeId;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getProcessId()
    {
        return processId;
    }

    public void setProcessId(Long processId)
    {
        this.processId = processId;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    public Integer getNodeOrder()
    {
        return nodeOrder;
    }

    public void setNodeOrder(Integer nodeOrder)
    {
        this.nodeOrder = nodeOrder;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public List<ApprovalNodeApprover> getApprovers()
    {
        return approvers;
    }

    public void setApprovers(List<ApprovalNodeApprover> approvers)
    {
        this.approvers = approvers;
    }
}
