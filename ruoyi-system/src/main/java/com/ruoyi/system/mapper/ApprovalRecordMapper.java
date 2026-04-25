package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.ApprovalRecord;
import java.util.List;

public interface ApprovalRecordMapper
{
    public List<ApprovalRecord> selectApprovalRecordsByInstanceId(Long instanceId);

    public int insertApprovalRecord(ApprovalRecord record);
}
