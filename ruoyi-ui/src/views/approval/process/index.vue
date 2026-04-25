<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="流程名称" prop="processName">
        <el-input v-model="queryParams.processName" placeholder="请输入流程名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="业务类型" prop="businessType">
        <el-input v-model="queryParams.businessType" placeholder="请输入业务类型" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="流程状态" clearable>
          <el-option label="启用" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['approval:process:add']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="processList">
      <el-table-column label="流程ID" align="center" prop="processId" width="90" />
      <el-table-column label="流程名称" align="center" prop="processName" :show-overflow-tooltip="true" />
      <el-table-column label="业务类型" align="center" prop="businessType" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['approval:process:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-setting" @click="handleNodes(scope.row)" v-hasPermi="['approval:process:edit']">节点</el-button>
          <el-button v-if="scope.row.status !== '0'" size="mini" type="text" icon="el-icon-check" @click="handleEnable(scope.row)" v-hasPermi="['approval:process:enable']">启用</el-button>
          <el-button v-else size="mini" type="text" icon="el-icon-close" @click="handleDisable(scope.row)" v-hasPermi="['approval:process:enable']">停用</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['approval:process:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="流程名称" prop="processName">
          <el-input v-model="form.processName" placeholder="请输入流程名称" />
        </el-form-item>
        <el-form-item label="业务类型" prop="businessType">
          <el-input v-model="form.businessType" placeholder="例如 leave、contract" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确定</el-button>
        <el-button @click="cancel">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="配置审批节点" :visible.sync="nodesOpen" width="860px" append-to-body>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="addNode">新增节点</el-button>
        </el-col>
      </el-row>
      <el-table :data="nodes">
        <el-table-column label="顺序" width="90">
          <template slot-scope="scope">
            <el-input-number v-model="scope.row.nodeOrder" size="mini" :min="1" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="节点名称">
          <template slot-scope="scope">
            <el-input v-model="scope.row.nodeName" size="small" placeholder="请输入节点名称" />
          </template>
        </el-table-column>
        <el-table-column label="指定用户" min-width="180">
          <template slot-scope="scope">
            <el-select v-model="scope.row.userIds" size="small" multiple filterable clearable placeholder="请选择用户" style="width: 100%">
              <el-option
                v-for="item in userOptions"
                :key="item.userId"
                :label="item.nickName ? item.nickName + '（' + item.userName + '）' : item.userName"
                :value="item.userId"
                :disabled="item.status === '1'"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="指定角色" min-width="180">
          <template slot-scope="scope">
            <el-select v-model="scope.row.roleIds" size="small" multiple filterable clearable placeholder="请选择角色" style="width: 100%">
              <el-option
                v-for="item in roleOptions"
                :key="item.roleId"
                :label="item.roleName"
                :value="item.roleId"
                :disabled="item.status === '1'"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-delete" @click="removeNode(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitNodes">确定</el-button>
        <el-button @click="nodesOpen = false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listProcess, addProcess, updateProcess, delProcess, enableProcess, disableProcess, listProcessNodes, saveProcessNodes } from '@/api/approval/process'
import { listUser } from '@/api/system/user'
import { listRole } from '@/api/system/role'

export default {
  name: 'ApprovalProcess',
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      processList: [],
      title: '',
      open: false,
      nodesOpen: false,
      currentProcessId: undefined,
      nodes: [],
      userOptions: [],
      roleOptions: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        processName: undefined,
        businessType: undefined,
        status: undefined
      },
      form: {},
      rules: {
        processName: [{ required: true, message: '流程名称不能为空', trigger: 'blur' }],
        businessType: [{ required: true, message: '业务类型不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    this.getApproverOptions()
  },
  methods: {
    getList() {
      this.loading = true
      listProcess(this.queryParams).then(response => {
        this.processList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        processId: undefined,
        processName: undefined,
        businessType: undefined,
        remark: undefined
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleAdd() {
      this.reset()
      this.open = true
      this.title = '新增审批流程'
    },
    handleUpdate(row) {
      this.form = Object.assign({}, row)
      this.open = true
      this.title = '修改审批流程'
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        const request = this.form.processId ? updateProcess(this.form) : addProcess(this.form)
        request.then(() => {
          this.$modal.msgSuccess('保存成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleEnable(row) {
      enableProcess(row.processId).then(() => {
        this.$modal.msgSuccess('启用成功')
        this.getList()
      })
    },
    handleDisable(row) {
      disableProcess(row.processId).then(() => {
        this.$modal.msgSuccess('停用成功')
        this.getList()
      })
    },
    handleDelete(row) {
      this.$modal.confirm('是否确认删除审批流程编号为 "' + row.processId + '" 的数据项？').then(function() {
        return delProcess(row.processId)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleNodes(row) {
      this.currentProcessId = row.processId
      this.getApproverOptions()
      listProcessNodes(row.processId).then(response => {
        this.nodes = (response.data || []).map((node, index) => this.toNodeForm(node, index))
        this.nodesOpen = true
      })
    },
    addNode() {
      this.nodes.push({
        nodeName: '',
        nodeOrder: this.nodes.length + 1,
        userIds: [],
        roleIds: []
      })
    },
    removeNode(index) {
      this.nodes.splice(index, 1)
    },
    submitNodes() {
      const data = this.nodes.map(node => {
        const approvers = []
        const userIds = node.userIds || []
        const roleIds = node.roleIds || []
        userIds.forEach(id => approvers.push({ approverType: 'USER', userId: id }))
        roleIds.forEach(id => approvers.push({ approverType: 'ROLE', roleId: id }))
        return {
          nodeName: node.nodeName,
          nodeOrder: node.nodeOrder,
          approvers: approvers
        }
      })
      saveProcessNodes(this.currentProcessId, data).then(() => {
        this.$modal.msgSuccess('保存成功')
        this.nodesOpen = false
      })
    },
    toNodeForm(node, index) {
      const approvers = node.approvers || []
      return {
        nodeName: node.nodeName,
        nodeOrder: node.nodeOrder || index + 1,
        userIds: approvers.filter(item => item.approverType === 'USER').map(item => item.userId),
        roleIds: approvers.filter(item => item.approverType === 'ROLE').map(item => item.roleId)
      }
    },
    getApproverOptions() {
      listUser({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.userOptions = response.rows || []
      })
      listRole({ pageNum: 1, pageSize: 1000 }).then(response => {
        this.roleOptions = response.rows || []
      })
    }
  }
}
</script>
