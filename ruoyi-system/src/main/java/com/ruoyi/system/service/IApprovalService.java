package com.ruoyi.system.service;

import com.ruoyi.system.domain.ApprovalInstance;
import com.ruoyi.system.domain.ApprovalNode;
import com.ruoyi.system.domain.ApprovalProcess;
import com.ruoyi.system.domain.ApprovalRecord;
import com.ruoyi.system.domain.ApprovalTask;
import com.ruoyi.system.domain.bo.ApprovalHandleRequest;
import com.ruoyi.system.domain.bo.ApprovalStartRequest;
import java.util.List;

public interface IApprovalService
{
    public int enableProcess(Long processId, String username);

    public int disableProcess(Long processId, String username);

    public int insertProcess(ApprovalProcess process, String username);

    public int updateProcess(ApprovalProcess process, String username);

    public int deleteProcessByIds(Long[] processIds);

    public List<ApprovalNode> selectNodes(Long processId);

    public int saveNodes(Long processId, List<ApprovalNode> nodes, String username);

    public ApprovalInstance startApproval(ApprovalStartRequest request, Long starterUserId, String username);

    public void approve(ApprovalHandleRequest request, Long userId, String username);

    public void reject(ApprovalHandleRequest request, Long userId, String username);

    public List<ApprovalProcess> selectProcessList(ApprovalProcess process);

    public List<ApprovalInstance> selectMyInstances(Long starterUserId);

    public ApprovalInstance selectMyInstanceById(Long instanceId, Long userId);

    public ApprovalInstance selectInstanceByBusiness(String businessType, String businessId);

    public List<ApprovalTask> selectPendingTasks(Long approverUserId);

    public List<ApprovalRecord> selectRecords(Long instanceId);
}
