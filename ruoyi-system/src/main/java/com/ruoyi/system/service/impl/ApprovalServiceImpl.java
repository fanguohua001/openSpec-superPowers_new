package com.ruoyi.system.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.constant.ApprovalConstants;
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
import com.ruoyi.system.service.IApprovalService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApprovalServiceImpl implements IApprovalService
{
    @Autowired
    private ApprovalProcessMapper processMapper;

    @Autowired
    private ApprovalNodeMapper nodeMapper;

    @Autowired
    private ApprovalNodeApproverMapper approverMapper;

    @Autowired
    private ApprovalInstanceMapper instanceMapper;

    @Autowired
    private ApprovalTaskMapper taskMapper;

    @Autowired
    private ApprovalRecordMapper recordMapper;

    @Autowired
    private SysUserMapper userMapper;

    public ApprovalServiceImpl()
    {
    }

    public ApprovalServiceImpl(ApprovalProcessMapper processMapper, ApprovalNodeMapper nodeMapper,
            ApprovalNodeApproverMapper approverMapper, ApprovalInstanceMapper instanceMapper,
            ApprovalTaskMapper taskMapper, ApprovalRecordMapper recordMapper, SysUserMapper userMapper)
    {
        this.processMapper = processMapper;
        this.nodeMapper = nodeMapper;
        this.approverMapper = approverMapper;
        this.instanceMapper = instanceMapper;
        this.taskMapper = taskMapper;
        this.recordMapper = recordMapper;
        this.userMapper = userMapper;
    }

    @Override
    public int enableProcess(Long processId, String username)
    {
        List<ApprovalNode> nodes = sortedNodes(processId);
        if (nodes.isEmpty())
        {
            throw new ServiceException("审批流程必须配置审批节点");
        }
        for (ApprovalNode node : nodes)
        {
            if (approverMapper.selectApprovalNodeApproversByNodeId(node.getNodeId()).isEmpty())
            {
                throw new ServiceException("审批节点必须配置审批人");
            }
        }
        ApprovalProcess process = new ApprovalProcess();
        process.setProcessId(processId);
        process.setStatus(ApprovalConstants.PROCESS_ENABLED);
        process.setUpdateBy(username);
        return processMapper.updateApprovalProcess(process);
    }

    @Override
    public int disableProcess(Long processId, String username)
    {
        ApprovalProcess process = new ApprovalProcess();
        process.setProcessId(processId);
        process.setStatus(ApprovalConstants.PROCESS_DISABLED);
        process.setUpdateBy(username);
        return processMapper.updateApprovalProcess(process);
    }

    @Override
    public int insertProcess(ApprovalProcess process, String username)
    {
        process.setStatus(ApprovalConstants.PROCESS_DISABLED);
        process.setCreateBy(username);
        return processMapper.insertApprovalProcess(process);
    }

    @Override
    public int updateProcess(ApprovalProcess process, String username)
    {
        process.setUpdateBy(username);
        return processMapper.updateApprovalProcess(process);
    }

    @Override
    public int deleteProcessByIds(Long[] processIds)
    {
        return processMapper.deleteApprovalProcessByIds(processIds);
    }

    @Override
    public List<ApprovalNode> selectNodes(Long processId)
    {
        List<ApprovalNode> nodes = sortedNodes(processId);
        for (ApprovalNode node : nodes)
        {
            node.setApprovers(approverMapper.selectApprovalNodeApproversByNodeId(node.getNodeId()));
        }
        return nodes;
    }

    @Override
    @Transactional
    public int saveNodes(Long processId, List<ApprovalNode> nodes, String username)
    {
        List<ApprovalNode> oldNodes = sortedNodes(processId);
        for (ApprovalNode oldNode : oldNodes)
        {
            approverMapper.deleteApprovalNodeApproversByNodeId(oldNode.getNodeId());
        }
        nodeMapper.deleteApprovalNodesByProcessId(processId);
        if (nodes == null)
        {
            return 0;
        }
        int rows = 0;
        int order = 1;
        for (ApprovalNode node : nodes)
        {
            node.setProcessId(processId);
            node.setNodeOrder(node.getNodeOrder() == null ? order : node.getNodeOrder());
            node.setStatus("0");
            node.setCreateBy(username);
            nodeMapper.insertApprovalNode(node);
            if (node.getApprovers() != null)
            {
                for (ApprovalNodeApprover approver : node.getApprovers())
                {
                    approver.setNodeId(node.getNodeId());
                    approver.setCreateBy(username);
                    approverMapper.insertApprovalNodeApprover(approver);
                }
            }
            rows++;
            order++;
        }
        return rows;
    }

    @Override
    @Transactional
    public ApprovalInstance startApproval(ApprovalStartRequest request, Long starterUserId, String username)
    {
        if (request == null || StringUtils.isEmpty(request.getBusinessType()))
        {
            throw new ServiceException("业务类型不能为空");
        }
        ApprovalProcess process = processMapper.selectEnabledApprovalProcessByBusinessType(request.getBusinessType());
        if (process == null)
        {
            throw new ServiceException("业务类型未配置启用的审批流程");
        }
        List<ApprovalNode> nodes = sortedNodes(process.getProcessId());
        if (nodes.isEmpty())
        {
            throw new ServiceException("审批流程必须配置审批节点");
        }
        ApprovalNode firstNode = nodes.get(0);
        ApprovalInstance instance = new ApprovalInstance();
        instance.setProcessId(process.getProcessId());
        instance.setBusinessType(request.getBusinessType());
        instance.setBusinessId(request.getBusinessId());
        instance.setBusinessTitle(request.getBusinessTitle());
        instance.setStarterUserId(starterUserId);
        instance.setCurrentNodeId(firstNode.getNodeId());
        instance.setStatus(ApprovalConstants.INSTANCE_PENDING);
        instance.setCreateBy(username);
        instanceMapper.insertApprovalInstance(instance);
        createTasks(instance.getInstanceId(), firstNode, username);
        return instance;
    }

    @Override
    @Transactional
    public void approve(ApprovalHandleRequest request, Long userId, String username)
    {
        ApprovalTask task = requirePendingOwnedTask(request, userId);
        ApprovalInstance instance = requireInstance(task.getInstanceId());
        List<ApprovalNode> nodes = sortedNodes(instance.getProcessId());
        ApprovalNode currentNode = findNode(nodes, task.getNodeId());
        ApprovalNode nextNode = findNextNode(nodes, currentNode);

        taskMapper.updateApprovalTaskStatus(task.getTaskId(), ApprovalConstants.TASK_APPROVED, username);
        taskMapper.closePendingTasksByInstanceAndNode(task.getInstanceId(), task.getNodeId(), task.getTaskId(), username);
        insertRecord(task, ApprovalConstants.ACTION_APPROVE, request.getComment(), userId, task.getNodeId(),
                nextNode == null ? null : nextNode.getNodeId(), username);

        if (nextNode == null)
        {
            instance.setStatus(ApprovalConstants.INSTANCE_APPROVED);
        }
        else
        {
            instance.setCurrentNodeId(nextNode.getNodeId());
            instance.setStatus(ApprovalConstants.INSTANCE_PENDING);
        }
        instance.setUpdateBy(username);
        instanceMapper.updateApprovalInstance(instance);
        if (nextNode != null)
        {
            createTasks(instance.getInstanceId(), nextNode, username);
        }
    }

    @Override
    @Transactional
    public void reject(ApprovalHandleRequest request, Long userId, String username)
    {
        ApprovalTask task = requirePendingOwnedTask(request, userId);
        ApprovalInstance instance = requireInstance(task.getInstanceId());
        List<ApprovalNode> nodes = sortedNodes(instance.getProcessId());
        ApprovalNode currentNode = findNode(nodes, task.getNodeId());
        ApprovalNode previousNode = findPreviousNode(nodes, currentNode);

        taskMapper.updateApprovalTaskStatus(task.getTaskId(), ApprovalConstants.TASK_REJECTED, username);
        taskMapper.closePendingTasksByInstanceAndNode(task.getInstanceId(), task.getNodeId(), task.getTaskId(), username);
        insertRecord(task, ApprovalConstants.ACTION_REJECT, request.getComment(), userId, task.getNodeId(),
                previousNode == null ? null : previousNode.getNodeId(), username);

        if (previousNode == null)
        {
            instance.setStatus(ApprovalConstants.INSTANCE_REJECTED);
        }
        else
        {
            instance.setCurrentNodeId(previousNode.getNodeId());
            instance.setStatus(ApprovalConstants.INSTANCE_PENDING);
        }
        instance.setUpdateBy(username);
        instanceMapper.updateApprovalInstance(instance);
        if (previousNode != null)
        {
            createTasks(instance.getInstanceId(), previousNode, username);
        }
    }

    @Override
    public List<ApprovalProcess> selectProcessList(ApprovalProcess process)
    {
        return processMapper.selectApprovalProcessList(process);
    }

    @Override
    public List<ApprovalInstance> selectMyInstances(Long starterUserId)
    {
        ApprovalInstance query = new ApprovalInstance();
        query.setStarterUserId(starterUserId);
        return instanceMapper.selectApprovalInstanceList(query);
    }

    @Override
    public ApprovalInstance selectMyInstanceById(Long instanceId, Long userId)
    {
        ApprovalInstance instance = instanceMapper.selectApprovalInstanceById(instanceId);
        if (instance == null || !userId.equals(instance.getStarterUserId()))
        {
            throw new ServiceException("无权访问该审批申请");
        }
        return instance;
    }

    @Override
    public ApprovalInstance selectInstanceByBusiness(String businessType, String businessId)
    {
        return instanceMapper.selectApprovalInstanceByBusiness(businessType, businessId);
    }

    @Override
    public List<ApprovalTask> selectPendingTasks(Long approverUserId)
    {
        ApprovalTask query = new ApprovalTask();
        query.setApproverUserId(approverUserId);
        query.setStatus(ApprovalConstants.TASK_PENDING);
        return taskMapper.selectApprovalTaskList(query);
    }

    @Override
    public List<ApprovalRecord> selectRecords(Long instanceId)
    {
        return recordMapper.selectApprovalRecordsByInstanceId(instanceId);
    }

    private ApprovalTask requirePendingOwnedTask(ApprovalHandleRequest request, Long userId)
    {
        if (request == null || request.getTaskId() == null)
        {
            throw new ServiceException("审批任务不能为空");
        }
        ApprovalTask task = taskMapper.selectApprovalTaskById(request.getTaskId());
        if (task == null)
        {
            throw new ServiceException("审批任务不存在");
        }
        if (!userId.equals(task.getApproverUserId()))
        {
            throw new ServiceException("无权处理该审批任务");
        }
        if (!ApprovalConstants.TASK_PENDING.equals(task.getStatus()))
        {
            throw new ServiceException("审批任务已处理");
        }
        return task;
    }

    private ApprovalInstance requireInstance(Long instanceId)
    {
        ApprovalInstance instance = instanceMapper.selectApprovalInstanceById(instanceId);
        if (instance == null)
        {
            throw new ServiceException("审批实例不存在");
        }
        return instance;
    }

    private List<ApprovalNode> sortedNodes(Long processId)
    {
        List<ApprovalNode> nodes = nodeMapper.selectApprovalNodesByProcessId(processId);
        if (nodes == null)
        {
            return Collections.emptyList();
        }
        nodes = new ArrayList<>(nodes);
        Collections.sort(nodes, Comparator.comparing(ApprovalNode::getNodeOrder));
        return nodes;
    }

    private ApprovalNode findNode(List<ApprovalNode> nodes, Long nodeId)
    {
        for (ApprovalNode node : nodes)
        {
            if (nodeId.equals(node.getNodeId()))
            {
                return node;
            }
        }
        throw new ServiceException("审批节点不存在");
    }

    private ApprovalNode findNextNode(List<ApprovalNode> nodes, ApprovalNode currentNode)
    {
        int index = nodes.indexOf(currentNode);
        return index >= 0 && index + 1 < nodes.size() ? nodes.get(index + 1) : null;
    }

    private ApprovalNode findPreviousNode(List<ApprovalNode> nodes, ApprovalNode currentNode)
    {
        int index = nodes.indexOf(currentNode);
        return index > 0 ? nodes.get(index - 1) : null;
    }

    private void createTasks(Long instanceId, ApprovalNode node, String username)
    {
        Set<Long> approverUserIds = resolveApproverUserIds(node.getNodeId());
        if (approverUserIds.isEmpty())
        {
            throw new ServiceException("审批节点必须配置审批人");
        }
        for (Long approverUserId : approverUserIds)
        {
            ApprovalTask task = new ApprovalTask();
            task.setInstanceId(instanceId);
            task.setNodeId(node.getNodeId());
            task.setApproverUserId(approverUserId);
            task.setStatus(ApprovalConstants.TASK_PENDING);
            task.setCreateBy(username);
            taskMapper.insertApprovalTask(task);
        }
    }

    private Set<Long> resolveApproverUserIds(Long nodeId)
    {
        List<ApprovalNodeApprover> approvers = approverMapper.selectApprovalNodeApproversByNodeId(nodeId);
        Set<Long> userIds = new LinkedHashSet<>();
        for (ApprovalNodeApprover approver : approvers)
        {
            if (ApprovalConstants.APPROVER_USER.equals(approver.getApproverType()) && approver.getUserId() != null)
            {
                userIds.add(approver.getUserId());
            }
            else if (ApprovalConstants.APPROVER_ROLE.equals(approver.getApproverType()) && approver.getRoleId() != null)
            {
                List<Long> roleUserIds = userMapper.selectUserIdsByRoleId(approver.getRoleId());
                if (roleUserIds != null)
                {
                    userIds.addAll(roleUserIds);
                }
            }
        }
        return userIds;
    }

    private void insertRecord(ApprovalTask task, String action, String comment, Long operatorUserId,
            Long fromNodeId, Long toNodeId, String username)
    {
        ApprovalRecord record = new ApprovalRecord();
        record.setInstanceId(task.getInstanceId());
        record.setTaskId(task.getTaskId());
        record.setFromNodeId(fromNodeId);
        record.setToNodeId(toNodeId);
        record.setAction(action);
        record.setComment(comment);
        record.setOperatorUserId(operatorUserId);
        record.setCreateBy(username);
        recordMapper.insertApprovalRecord(record);
    }
}
