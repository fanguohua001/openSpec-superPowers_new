package com.ruoyi.web.controller.approval;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.ApprovalNode;
import com.ruoyi.system.domain.ApprovalProcess;
import com.ruoyi.system.service.IApprovalService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/approval/process")
public class ApprovalProcessController extends BaseController
{
    @Autowired
    private IApprovalService approvalService;

    @PreAuthorize("@ss.hasPermi('approval:process:list')")
    @GetMapping("/list")
    public TableDataInfo list(ApprovalProcess process)
    {
        startPage();
        List<ApprovalProcess> list = approvalService.selectProcessList(process);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('approval:process:add')")
    @Log(title = "审批流程", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ApprovalProcess process)
    {
        return toAjax(approvalService.insertProcess(process, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('approval:process:edit')")
    @Log(title = "审批流程", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ApprovalProcess process)
    {
        return toAjax(approvalService.updateProcess(process, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('approval:process:enable')")
    @Log(title = "审批流程", businessType = BusinessType.UPDATE)
    @PutMapping("/{processId}/enable")
    public AjaxResult enable(@PathVariable Long processId)
    {
        return toAjax(approvalService.enableProcess(processId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('approval:process:enable')")
    @Log(title = "审批流程", businessType = BusinessType.UPDATE)
    @PutMapping("/{processId}/disable")
    public AjaxResult disable(@PathVariable Long processId)
    {
        return toAjax(approvalService.disableProcess(processId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('approval:process:query')")
    @GetMapping("/{processId}/nodes")
    public AjaxResult nodes(@PathVariable Long processId)
    {
        return success(approvalService.selectNodes(processId));
    }

    @PreAuthorize("@ss.hasPermi('approval:process:edit')")
    @Log(title = "审批节点", businessType = BusinessType.UPDATE)
    @PutMapping("/{processId}/nodes")
    public AjaxResult saveNodes(@PathVariable Long processId, @RequestBody List<ApprovalNode> nodes)
    {
        return toAjax(approvalService.saveNodes(processId, nodes, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('approval:process:remove')")
    @Log(title = "审批流程", businessType = BusinessType.DELETE)
    @DeleteMapping("/{processIds}")
    public AjaxResult remove(@PathVariable Long[] processIds)
    {
        return toAjax(approvalService.deleteProcessByIds(processIds));
    }
}
