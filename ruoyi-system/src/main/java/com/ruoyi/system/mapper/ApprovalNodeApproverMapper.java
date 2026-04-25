package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.ApprovalNodeApprover;
import java.util.List;

public interface ApprovalNodeApproverMapper
{
    public List<ApprovalNodeApprover> selectApprovalNodeApproversByNodeId(Long nodeId);

    public int insertApprovalNodeApprover(ApprovalNodeApprover approver);

    public int deleteApprovalNodeApproversByNodeId(Long nodeId);
}
