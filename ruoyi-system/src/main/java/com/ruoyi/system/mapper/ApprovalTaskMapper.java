package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.ApprovalTask;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApprovalTaskMapper
{
    public ApprovalTask selectApprovalTaskById(Long taskId);

    public List<ApprovalTask> selectApprovalTaskList(ApprovalTask task);

    public int insertApprovalTask(ApprovalTask task);

    public int updateApprovalTask(ApprovalTask task);

    public int updateApprovalTaskStatus(@Param("taskId") Long taskId, @Param("status") String status,
            @Param("updateBy") String updateBy);

    public int closePendingTasksByInstanceAndNode(@Param("instanceId") Long instanceId,
            @Param("nodeId") Long nodeId, @Param("excludeTaskId") Long excludeTaskId,
            @Param("updateBy") String updateBy);
}
