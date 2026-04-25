<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <right-toolbar @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="taskList">
      <el-table-column label="流程名称" align="center" prop="processName" :show-overflow-tooltip="true" />
      <el-table-column label="业务标题" align="center" prop="businessTitle" :show-overflow-tooltip="true" />
      <el-table-column label="业务类型" align="center" prop="businessType" width="120" />
      <el-table-column label="当前节点" align="center" prop="nodeName" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="发起人" align="center" prop="starterUserName" width="160" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag size="mini">{{ formatStatus(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="180">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-check" @click="handleApprove(scope.row)" v-hasPermi="['approval:task:approve']">同意</el-button>
          <el-button size="mini" type="text" icon="el-icon-close" @click="handleReject(scope.row)" v-hasPermi="['approval:task:reject']">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="handleTitle" :visible.sync="handleOpen" width="520px" append-to-body>
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="审批意见">
          <el-input v-model="handleForm.comment" type="textarea" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitHandle">确定</el-button>
        <el-button @click="handleOpen = false">取消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPendingTask, approveTask, rejectTask } from '@/api/approval/task'

export default {
  name: 'ApprovalTask',
  data() {
    return {
      loading: true,
      total: 0,
      taskList: [],
      handleOpen: false,
      handleTitle: '',
      handleAction: '',
      handleForm: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listPendingTask(this.queryParams).then(response => {
        this.taskList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleApprove(row) {
      this.handleTitle = '同意审批'
      this.handleAction = 'approve'
      this.handleForm = { taskId: row.taskId, comment: '' }
      this.handleOpen = true
    },
    handleReject(row) {
      this.handleTitle = '驳回审批'
      this.handleAction = 'reject'
      this.handleForm = { taskId: row.taskId, comment: '' }
      this.handleOpen = true
    },
    submitHandle() {
      const request = this.handleAction === 'approve' ? approveTask(this.handleForm) : rejectTask(this.handleForm)
      request.then(() => {
        this.$modal.msgSuccess('处理成功')
        this.handleOpen = false
        this.getList()
      })
    },
    formatStatus(status) {
      const statusMap = {
        pending: '待审批',
        approved: '已同意',
        rejected: '已驳回',
        closed: '已关闭'
      }
      return statusMap[status] || status
    }
  }
}
</script>
