package com.ruoyi.web.controller.approval;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.ApprovalTask;
import com.ruoyi.system.domain.bo.ApprovalHandleRequest;
import com.ruoyi.system.service.IApprovalService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/approval/task")
public class ApprovalTaskController extends BaseController
{
    @Autowired
    private IApprovalService approvalService;

    @PreAuthorize("@ss.hasPermi('approval:task:list')")
    @GetMapping("/pending/list")
    public TableDataInfo pendingList()
    {
        startPage();
        List<ApprovalTask> list = approvalService.selectPendingTasks(getUserId());
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('approval:task:approve')")
    @Log(title = "审批任务", businessType = BusinessType.UPDATE)
    @PostMapping("/approve")
    public AjaxResult approve(@RequestBody ApprovalHandleRequest request)
    {
        approvalService.approve(request, getUserId(), getUsername());
        return success();
    }

    @PreAuthorize("@ss.hasPermi('approval:task:reject')")
    @Log(title = "审批任务", businessType = BusinessType.UPDATE)
    @PostMapping("/reject")
    public AjaxResult reject(@RequestBody ApprovalHandleRequest request)
    {
        approvalService.reject(request, getUserId(), getUsername());
        return success();
    }
}
