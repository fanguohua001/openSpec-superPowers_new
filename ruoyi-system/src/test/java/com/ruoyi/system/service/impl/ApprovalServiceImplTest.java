package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.ApprovalInstance;
import com.ruoyi.system.domain.ApprovalNode;
import com.ruoyi.system.domain.ApprovalNodeApprover;
import com.ruoyi.system.domain.ApprovalProcess;
import com.ruoyi.system.domain.ApprovalRecord;
import com.ruoyi.system.domain.ApprovalTask;
import com.ruoyi.system.domain.bo.ApprovalHandleRequest;
import com.ruoyi.system.domain.bo.ApprovalStartRequest;
import com.ruoyi.system.mapper.ApprovalInstanceMapper;
import com.ruoyi.system.mapper.ApprovalNodeApproverMapper;
import com.ruoyi.system.mapper.ApprovalNodeMapper;
import com.ruoyi.system.mapper.ApprovalProcessMapper;
import com.ruoyi.system.mapper.ApprovalRecordMapper;
import com.ruoyi.system.mapper.ApprovalTaskMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApprovalServiceImplTest
{
    private ApprovalProcessMapper processMapper;

    private ApprovalNodeMapper nodeMapper;

    private ApprovalNodeApproverMapper approverMapper;

    private ApprovalInstanceMapper instanceMapper;

    private ApprovalTaskMapper taskMapper;

    private ApprovalRecordMapper recordMapper;

    private SysUserMapper userMapper;

    private ApprovalServiceImpl service;

    @Before
    public void setUp()
    {
        processMapper = mock(ApprovalProcessMapper.class);
        nodeMapper = mock(ApprovalNodeMapper.class);
        approverMapper = mock(ApprovalNodeApproverMapper.class);
        instanceMapper = mock(ApprovalInstanceMapper.class);
        taskMapper = mock(ApprovalTaskMapper.class);
        recordMapper = mock(ApprovalRecordMapper.class);
        userMapper = mock(SysUserMapper.class);
        service = new ApprovalServiceImpl(processMapper, nodeMapper, approverMapper,
                instanceMapper, taskMapper, recordMapper, userMapper);
    }

    @Test
    public void enableProcessShouldRejectNodeWithoutApprover()
    {
        when(nodeMapper.selectApprovalNodesByProcessId(10L)).thenReturn(Arrays.asList(node(100L, 1), node(101L, 2)));
        when(approverMapper.selectApprovalNodeApproversByNodeId(100L))
                .thenReturn(Collections.singletonList(userApprover(1L)));
        when(approverMapper.selectApprovalNodeApproversByNodeId(101L)).thenReturn(Collections.emptyList());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.enableProcess(10L, "admin"));

        assertEquals("审批节点必须配置审批人", exception.getMessage());
        verify(processMapper, never()).updateApprovalProcess(any());
    }

    @Test
    public void startApprovalShouldCreateFirstNodeTasksForUsersAndRoles()
    {
        ApprovalProcess process = process(10L, "leave");
        when(processMapper.selectEnabledApprovalProcessByBusinessType("leave")).thenReturn(process);
        when(nodeMapper.selectApprovalNodesByProcessId(10L)).thenReturn(Arrays.asList(node(100L, 1), node(101L, 2)));
        when(approverMapper.selectApprovalNodeApproversByNodeId(100L))
                .thenReturn(Arrays.asList(userApprover(2L), roleApprover(5L)));
        when(userMapper.selectUserIdsByRoleId(5L)).thenReturn(Arrays.asList(3L, 4L));
        when(instanceMapper.insertApprovalInstance(any())).thenAnswer(invocation -> {
            ApprovalInstance instance = invocation.getArgument(0);
            instance.setInstanceId(200L);
            return 1;
        });

        ApprovalStartRequest request = new ApprovalStartRequest();
        request.setBusinessType("leave");
        request.setBusinessId("L-001");
        request.setBusinessTitle("请假申请");

        ApprovalInstance instance = service.startApproval(request, 1L, "zhangsan");

        assertEquals(Long.valueOf(200L), instance.getInstanceId());
        assertEquals("pending", instance.getStatus());
        assertEquals(Long.valueOf(100L), instance.getCurrentNodeId());

        ArgumentCaptor<ApprovalTask> taskCaptor = ArgumentCaptor.forClass(ApprovalTask.class);
        verify(taskMapper, org.mockito.Mockito.times(3)).insertApprovalTask(taskCaptor.capture());
        List<ApprovalTask> tasks = taskCaptor.getAllValues();
        assertEquals(Long.valueOf(2L), tasks.get(0).getApproverUserId());
        assertEquals(Long.valueOf(3L), tasks.get(1).getApproverUserId());
        assertEquals(Long.valueOf(4L), tasks.get(2).getApproverUserId());
    }

    @Test
    public void approveShouldMoveToNextNodeAndCloseOtherCurrentTasks()
    {
        ApprovalTask task = task(300L, 200L, 100L, 2L);
        ApprovalInstance instance = instance(200L, 10L, 100L, "pending", 1L);
        when(taskMapper.selectApprovalTaskById(300L)).thenReturn(task);
        when(instanceMapper.selectApprovalInstanceById(200L)).thenReturn(instance);
        when(nodeMapper.selectApprovalNodesByProcessId(10L)).thenReturn(Arrays.asList(node(100L, 1), node(101L, 2)));
        when(approverMapper.selectApprovalNodeApproversByNodeId(101L))
                .thenReturn(Collections.singletonList(userApprover(4L)));

        ApprovalHandleRequest request = new ApprovalHandleRequest();
        request.setTaskId(300L);
        request.setComment("同意");

        service.approve(request, 2L, "lisi");

        verify(taskMapper).updateApprovalTaskStatus(300L, "approved", "lisi");
        verify(taskMapper).closePendingTasksByInstanceAndNode(200L, 100L, 300L, "lisi");

        ArgumentCaptor<ApprovalInstance> instanceCaptor = ArgumentCaptor.forClass(ApprovalInstance.class);
        verify(instanceMapper).updateApprovalInstance(instanceCaptor.capture());
        assertEquals(Long.valueOf(101L), instanceCaptor.getValue().getCurrentNodeId());
        assertEquals("pending", instanceCaptor.getValue().getStatus());

        ArgumentCaptor<ApprovalTask> taskCaptor = ArgumentCaptor.forClass(ApprovalTask.class);
        verify(taskMapper).insertApprovalTask(taskCaptor.capture());
        assertEquals(Long.valueOf(4L), taskCaptor.getValue().getApproverUserId());
    }

    @Test
    public void approveLastNodeShouldCompleteInstance()
    {
        ApprovalTask task = task(300L, 200L, 101L, 2L);
        ApprovalInstance instance = instance(200L, 10L, 101L, "pending", 1L);
        when(taskMapper.selectApprovalTaskById(300L)).thenReturn(task);
        when(instanceMapper.selectApprovalInstanceById(200L)).thenReturn(instance);
        when(nodeMapper.selectApprovalNodesByProcessId(10L)).thenReturn(Arrays.asList(node(100L, 1), node(101L, 2)));

        ApprovalHandleRequest request = new ApprovalHandleRequest();
        request.setTaskId(300L);

        service.approve(request, 2L, "lisi");

        ArgumentCaptor<ApprovalInstance> instanceCaptor = ArgumentCaptor.forClass(ApprovalInstance.class);
        verify(instanceMapper).updateApprovalInstance(instanceCaptor.capture());
        assertEquals("approved", instanceCaptor.getValue().getStatus());
    }

    @Test
    public void rejectNonFirstNodeShouldMoveBackToPreviousNode()
    {
        ApprovalTask task = task(300L, 200L, 101L, 2L);
        ApprovalInstance instance = instance(200L, 10L, 101L, "pending", 1L);
        when(taskMapper.selectApprovalTaskById(300L)).thenReturn(task);
        when(instanceMapper.selectApprovalInstanceById(200L)).thenReturn(instance);
        when(nodeMapper.selectApprovalNodesByProcessId(10L)).thenReturn(Arrays.asList(node(100L, 1), node(101L, 2)));
        when(approverMapper.selectApprovalNodeApproversByNodeId(100L))
                .thenReturn(Collections.singletonList(userApprover(5L)));

        ApprovalHandleRequest request = new ApprovalHandleRequest();
        request.setTaskId(300L);
        request.setComment("退回修改");

        service.reject(request, 2L, "lisi");

        ArgumentCaptor<ApprovalInstance> instanceCaptor = ArgumentCaptor.forClass(ApprovalInstance.class);
        verify(instanceMapper).updateApprovalInstance(instanceCaptor.capture());
        assertEquals(Long.valueOf(100L), instanceCaptor.getValue().getCurrentNodeId());
        assertEquals("pending", instanceCaptor.getValue().getStatus());

        ArgumentCaptor<ApprovalTask> taskCaptor = ArgumentCaptor.forClass(ApprovalTask.class);
        verify(taskMapper).insertApprovalTask(taskCaptor.capture());
        assertEquals(Long.valueOf(5L), taskCaptor.getValue().getApproverUserId());
    }

    @Test
    public void rejectFirstNodeShouldRejectInstance()
    {
        ApprovalTask task = task(300L, 200L, 100L, 2L);
        ApprovalInstance instance = instance(200L, 10L, 100L, "pending", 1L);
        when(taskMapper.selectApprovalTaskById(300L)).thenReturn(task);
        when(instanceMapper.selectApprovalInstanceById(200L)).thenReturn(instance);
        when(nodeMapper.selectApprovalNodesByProcessId(10L)).thenReturn(Arrays.asList(node(100L, 1), node(101L, 2)));

        ApprovalHandleRequest request = new ApprovalHandleRequest();
        request.setTaskId(300L);

        service.reject(request, 2L, "lisi");

        ArgumentCaptor<ApprovalInstance> instanceCaptor = ArgumentCaptor.forClass(ApprovalInstance.class);
        verify(instanceMapper).updateApprovalInstance(instanceCaptor.capture());
        assertEquals("rejected", instanceCaptor.getValue().getStatus());
    }

    @Test
    public void approveShouldRejectTaskNotOwnedByCurrentUser()
    {
        when(taskMapper.selectApprovalTaskById(300L)).thenReturn(task(300L, 200L, 100L, 9L));

        ApprovalHandleRequest request = new ApprovalHandleRequest();
        request.setTaskId(300L);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.approve(request, 2L, "lisi"));

        assertEquals("无权处理该审批任务", exception.getMessage());
        verify(recordMapper, never()).insertApprovalRecord(any());
    }

    @Test
    public void selectMyInstanceShouldRejectOtherUsersInstance()
    {
        ApprovalInstance instance = instance(200L, 10L, 100L, "pending", 1L);
        when(instanceMapper.selectApprovalInstanceById(200L)).thenReturn(instance);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.selectMyInstanceById(200L, 2L));

        assertEquals("无权访问该审批申请", exception.getMessage());
    }

    @Test
    public void approveShouldWriteApprovalRecord()
    {
        ApprovalTask task = task(300L, 200L, 101L, 2L);
        ApprovalInstance instance = instance(200L, 10L, 101L, "pending", 1L);
        when(taskMapper.selectApprovalTaskById(300L)).thenReturn(task);
        when(instanceMapper.selectApprovalInstanceById(200L)).thenReturn(instance);
        when(nodeMapper.selectApprovalNodesByProcessId(10L)).thenReturn(Arrays.asList(node(100L, 1), node(101L, 2)));

        ApprovalHandleRequest request = new ApprovalHandleRequest();
        request.setTaskId(300L);
        request.setComment("同意");

        service.approve(request, 2L, "lisi");

        ArgumentCaptor<ApprovalRecord> recordCaptor = ArgumentCaptor.forClass(ApprovalRecord.class);
        verify(recordMapper).insertApprovalRecord(recordCaptor.capture());
        assertEquals("approve", recordCaptor.getValue().getAction());
        assertEquals("同意", recordCaptor.getValue().getComment());
        assertEquals(Long.valueOf(2L), recordCaptor.getValue().getOperatorUserId());
        assertEquals(Long.valueOf(101L), recordCaptor.getValue().getFromNodeId());
    }

    private ApprovalProcess process(Long processId, String businessType)
    {
        ApprovalProcess process = new ApprovalProcess();
        process.setProcessId(processId);
        process.setBusinessType(businessType);
        process.setStatus("0");
        return process;
    }

    private ApprovalNode node(Long nodeId, Integer order)
    {
        ApprovalNode node = new ApprovalNode();
        node.setNodeId(nodeId);
        node.setNodeOrder(order);
        return node;
    }

    private ApprovalNodeApprover userApprover(Long userId)
    {
        ApprovalNodeApprover approver = new ApprovalNodeApprover();
        approver.setApproverType("USER");
        approver.setUserId(userId);
        return approver;
    }

    private ApprovalNodeApprover roleApprover(Long roleId)
    {
        ApprovalNodeApprover approver = new ApprovalNodeApprover();
        approver.setApproverType("ROLE");
        approver.setRoleId(roleId);
        return approver;
    }

    private ApprovalInstance instance(Long instanceId, Long processId, Long nodeId, String status, Long starterUserId)
    {
        ApprovalInstance instance = new ApprovalInstance();
        instance.setInstanceId(instanceId);
        instance.setProcessId(processId);
        instance.setCurrentNodeId(nodeId);
        instance.setStatus(status);
        instance.setStarterUserId(starterUserId);
        return instance;
    }

    private ApprovalTask task(Long taskId, Long instanceId, Long nodeId, Long approverUserId)
    {
        ApprovalTask task = new ApprovalTask();
        task.setTaskId(taskId);
        task.setInstanceId(instanceId);
        task.setNodeId(nodeId);
        task.setApproverUserId(approverUserId);
        task.setStatus("pending");
        return task;
    }
}
