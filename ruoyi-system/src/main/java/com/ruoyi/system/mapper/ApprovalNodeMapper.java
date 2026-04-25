package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.ApprovalNode;
import java.util.List;

public interface ApprovalNodeMapper
{
    public ApprovalNode selectApprovalNodeById(Long nodeId);

    public List<ApprovalNode> selectApprovalNodesByProcessId(Long processId);

    public int insertApprovalNode(ApprovalNode node);

    public int updateApprovalNode(ApprovalNode node);

    public int deleteApprovalNodesByProcessId(Long processId);
}
