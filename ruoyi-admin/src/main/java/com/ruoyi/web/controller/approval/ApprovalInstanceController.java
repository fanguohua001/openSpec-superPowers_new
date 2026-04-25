package com.ruoyi.web.controller.approval;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.ApprovalInstance;
import com.ruoyi.system.domain.bo.ApprovalStartRequest;
import com.ruoyi.system.service.IApprovalService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/approval/instance")
public class ApprovalInstanceController extends BaseController
{
    @Autowired
    private IApprovalService approvalService;

    @PreAuthorize("@ss.hasPermi('approval:instance:start')")
    @Log(title = "审批申请", businessType = BusinessType.INSERT)
    @PostMapping("/start")
    public AjaxResult start(@RequestBody ApprovalStartRequest request)
    {
        return success(approvalService.startApproval(request, getUserId(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('approval:instance:list')")
    @GetMapping("/my/list")
    public TableDataInfo myList()
    {
        startPage();
        List<ApprovalInstance> list = approvalService.selectMyInstances(getUserId());
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('approval:instance:query')")
    @GetMapping("/{instanceId}")
    public AjaxResult getInfo(@PathVariable Long instanceId)
    {
        AjaxResult result = success(approvalService.selectMyInstanceById(instanceId, getUserId()));
        result.put("records", approvalService.selectRecords(instanceId));
        return result;
    }

    @PreAuthorize("@ss.hasPermi('approval:instance:query')")
    @GetMapping("/business")
    public AjaxResult getByBusiness(@RequestParam String businessType, @RequestParam String businessId)
    {
        return success(approvalService.selectInstanceByBusiness(businessType, businessId));
    }
}
