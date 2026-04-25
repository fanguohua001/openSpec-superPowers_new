package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.ApprovalInstance;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ApprovalInstanceMapper
{
    public ApprovalInstance selectApprovalInstanceById(Long instanceId);

    public ApprovalInstance selectApprovalInstanceByBusiness(@Param("businessType") String businessType,
            @Param("businessId") String businessId);

    public List<ApprovalInstance> selectApprovalInstanceList(ApprovalInstance instance);

    public int insertApprovalInstance(ApprovalInstance instance);

    public int updateApprovalInstance(ApprovalInstance instance);
}
