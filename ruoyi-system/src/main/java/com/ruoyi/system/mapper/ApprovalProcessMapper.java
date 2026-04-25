package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.ApprovalProcess;
import java.util.List;

public interface ApprovalProcessMapper
{
    public ApprovalProcess selectApprovalProcessById(Long processId);

    public ApprovalProcess selectEnabledApprovalProcessByBusinessType(String businessType);

    public List<ApprovalProcess> selectApprovalProcessList(ApprovalProcess process);

    public int insertApprovalProcess(ApprovalProcess process);

    public int updateApprovalProcess(ApprovalProcess process);

    public int deleteApprovalProcessByIds(Long[] processIds);
}
