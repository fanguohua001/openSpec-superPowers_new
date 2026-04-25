<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleStart" v-hasPermi="['approval:instance:start']">发起审批</el-button>
      </el-col>
      <right-toolbar @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="instanceList">
      <el-table-column label="实例ID" align="center" prop="instanceId" width="90" />
      <el-table-column label="业务标题" align="center" prop="businessTitle" :show-overflow-tooltip="true" />
      <el-table-column label="业务类型" align="center" prop="businessType" />
      <el-table-column label="业务主键" align="center" prop="businessId" />
      <el-table-column label="状态" align="center" prop="status" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="120">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleDetail(scope.row)" v-hasPermi="['approval:instance:query']">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="发起审批" :visible.sync="startOpen" width="560px" append-to-body>
      <el-form ref="startForm" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="业务类型" prop="businessType">
          <el-input v-model="form.businessType" placeholder="请输入业务类型" />
        </el-form-item>
        <el-form-item label="业务主键" prop="businessId">
          <el-input v-model="form.businessId" placeholder="请输入业务主键" />
        </el-form-item>
        <el-form-item label="业务标题" prop="businessTitle">
          <el-input v-model="form.businessTitle" placeholder="请输入业务标题" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitStart">确定</el-button>
        <el-button @click="startOpen = false">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="审批详情" :visible.sync="detailOpen" width="760px" append-to-body>
      <el-descriptions :column="2" border size="small" v-if="detail">
        <el-descriptions-item label="业务标题">{{ detail.businessTitle }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ detail.businessType }}</el-descriptions-item>
        <el-descriptions-item label="业务主键">{{ detail.businessId }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="records" style="margin-top: 16px">
        <el-table-column label="动作" prop="action" width="100" />
        <el-table-column label="意见" prop="comment" :show-overflow-tooltip="true" />
        <el-table-column label="操作人" prop="operatorUserId" width="100" />
        <el-table-column label="时间" prop="createTime" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.createTime) }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { startApproval, listMyInstance, getInstance } from '@/api/approval/instance'

export default {
  name: 'ApprovalInstance',
  data() {
    return {
      loading: true,
      total: 0,
      instanceList: [],
      startOpen: false,
      detailOpen: false,
      detail: null,
      records: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10
      },
      form: {},
      rules: {
        businessType: [{ required: true, message: '业务类型不能为空', trigger: 'blur' }],
        businessId: [{ required: true, message: '业务主键不能为空', trigger: 'blur' }],
        businessTitle: [{ required: true, message: '业务标题不能为空', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listMyInstance(this.queryParams).then(response => {
        this.instanceList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleStart() {
      this.form = {}
      this.startOpen = true
    },
    submitStart() {
      this.$refs.startForm.validate(valid => {
        if (!valid) return
        startApproval(this.form).then(() => {
          this.$modal.msgSuccess('发起成功')
          this.startOpen = false
          this.getList()
        })
      })
    },
    handleDetail(row) {
      getInstance(row.instanceId).then(response => {
        this.detail = response.data
        this.records = response.records || []
        this.detailOpen = true
      })
    }
  }
}
</script>
