<template>
  <div class="app-container ai-chat-page">
    <aside class="session-panel">
      <div class="session-toolbar">
        <el-button
          type="primary"
          size="mini"
          icon="el-icon-plus"
          @click="startNewChat"
          v-hasPermi="['tool:aiChat:send']"
        >新会话</el-button>
        <el-tooltip content="刷新会话" placement="top">
          <el-button size="mini" icon="el-icon-refresh" circle @click="getSessions" />
        </el-tooltip>
      </div>

      <div class="session-list" v-loading="sessionLoading">
        <button
          v-for="item in sessions"
          :key="item.sessionId"
          class="session-item"
          :class="{ active: item.sessionId === activeSessionId }"
          @click="selectSession(item)"
        >
          <span class="session-title">{{ item.title || '未命名会话' }}</span>
          <span class="session-meta">{{ item.updateTime || item.createTime }}</span>
          <el-tooltip content="删除" placement="top">
            <i
              class="el-icon-delete session-delete"
              v-hasPermi="['tool:aiChat:remove']"
              @click.stop="removeSession(item)"
            />
          </el-tooltip>
        </button>
        <el-empty v-if="!sessionLoading && sessions.length === 0" description="暂无会话" :image-size="76" />
      </div>
    </aside>

    <main class="chat-panel">
      <header class="chat-header">
        <div>
          <h2>{{ activeTitle }}</h2>
          <span>{{ messages.length }} 条消息</span>
        </div>
        <el-tag v-if="sending" size="mini" type="warning">回复中</el-tag>
      </header>

      <section ref="messageBody" class="message-body" v-loading="messageLoading">
        <el-empty
          v-if="!messageLoading && messages.length === 0"
          description="开始新的对话"
          :image-size="96"
        />
        <div
          v-for="message in messages"
          :key="message.messageId || message.localId"
          class="message-row"
          :class="'is-' + message.role"
        >
          <div class="message-avatar">{{ message.role === 'user' ? '我' : 'AI' }}</div>
          <div class="message-bubble">
            <div class="message-meta">
              <span>{{ message.role === 'user' ? '我' : 'AI助手' }}</span>
              <el-tag v-if="message.status === '1'" size="mini" type="info">生成中</el-tag>
              <el-tag v-if="message.status === '2'" size="mini" type="danger">失败</el-tag>
            </div>
            <div class="message-content">
              {{ message.content }}
              <span v-if="message.status === '1'" class="typing-caret"></span>
            </div>
            <div v-if="message.errorMessage" class="message-error">{{ message.errorMessage }}</div>
          </div>
        </div>
      </section>

      <footer class="compose-bar">
        <el-input
          v-model="input"
          type="textarea"
          resize="none"
          :autosize="{ minRows: 2, maxRows: 5 }"
          maxlength="4000"
          placeholder="输入消息"
          @keydown.native="handleKeydown"
        />
        <div class="compose-actions">
          <span class="input-count">{{ input.length }}/4000</span>
          <el-button
            v-if="sending"
            size="mini"
            icon="el-icon-close"
            @click="stopStream"
          >停止</el-button>
          <el-button
            type="primary"
            size="mini"
            icon="el-icon-s-promotion"
            :disabled="!canSend"
            @click="send"
            v-hasPermi="['tool:aiChat:send']"
          >发送</el-button>
        </div>
      </footer>
    </main>
  </div>
</template>

<script>
import { deleteSession, listMessages, listSessions, streamMessage } from '@/api/tool/aiChat'

export default {
  name: 'AiChat',
  data() {
    return {
      sessionLoading: false,
      messageLoading: false,
      sending: false,
      sessions: [],
      messages: [],
      activeSessionId: undefined,
      input: '',
      streamTask: null,
      currentAssistantMessage: null,
      queryParams: {
        pageNum: 1,
        pageSize: 50
      }
    }
  },
  computed: {
    activeTitle() {
      const session = this.sessions.find(item => item.sessionId === this.activeSessionId)
      return session ? session.title : 'AI聊天'
    },
    canSend() {
      return !this.sending && this.input.trim().length > 0
    }
  },
  created() {
    this.getSessions()
  },
  beforeDestroy() {
    this.stopStream()
  },
  methods: {
    getSessions() {
      this.sessionLoading = true
      listSessions(this.queryParams).then(response => {
        this.sessions = response.rows || []
      }).finally(() => {
        this.sessionLoading = false
      })
    },
    selectSession(session) {
      if (this.sending) {
        return
      }
      this.activeSessionId = session.sessionId
      this.messageLoading = true
      listMessages(session.sessionId).then(response => {
        this.messages = response.data || []
        this.scrollToBottom()
      }).finally(() => {
        this.messageLoading = false
      })
    },
    startNewChat() {
      if (this.sending) {
        return
      }
      this.activeSessionId = undefined
      this.messages = []
      this.input = ''
      this.currentAssistantMessage = null
    },
    removeSession(session) {
      this.$modal.confirm('是否确认删除会话"' + (session.title || session.sessionId) + '"？').then(() => {
        return deleteSession(session.sessionId)
      }).then(() => {
        if (this.activeSessionId === session.sessionId) {
          this.startNewChat()
        }
        this.getSessions()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    handleKeydown(event) {
      if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault()
        this.send()
      }
    },
    send() {
      if (!this.canSend) {
        return
      }
      const content = this.input.trim()
      this.input = ''
      this.sending = true

      const userMessage = {
        localId: 'user-' + Date.now(),
        role: 'user',
        content,
        status: '0'
      }
      const assistantMessage = {
        localId: 'assistant-' + Date.now(),
        role: 'assistant',
        content: '',
        status: '1',
        errorMessage: ''
      }
      this.currentAssistantMessage = assistantMessage
      this.messages.push(userMessage, assistantMessage)
      this.scrollToBottom()

      this.streamTask = streamMessage({
        sessionId: this.activeSessionId,
        content
      }, {
        session: data => this.handleSessionEvent(data),
        message: data => this.handleMessageEvent(data, userMessage, assistantMessage),
        delta: data => this.handleDeltaEvent(data, assistantMessage),
        done: () => this.handleDoneEvent(assistantMessage),
        error: data => this.handleErrorEvent(data, assistantMessage)
      })

      this.streamTask.promise.catch(error => {
        if (error && error.name === 'AbortError') {
          this.handleErrorEvent('已停止当前回复', assistantMessage)
          return
        }
        this.handleErrorEvent(error.message || 'AI聊天请求失败', assistantMessage)
      }).finally(() => {
        this.sending = false
        this.streamTask = null
        this.currentAssistantMessage = null
        this.getSessions()
      })
    },
    handleSessionEvent(data) {
      if (data && data.sessionId) {
        this.activeSessionId = data.sessionId
      }
    },
    handleMessageEvent(data, userMessage, assistantMessage) {
      if (!data) {
        return
      }
      userMessage.messageId = data.userMessageId
      assistantMessage.messageId = data.assistantMessageId
    },
    handleDeltaEvent(data, assistantMessage) {
      assistantMessage.content += typeof data === 'string' ? data : ''
      this.scrollToBottom()
    },
    handleDoneEvent(assistantMessage) {
      assistantMessage.status = '0'
      this.scrollToBottom()
    },
    handleErrorEvent(data, assistantMessage) {
      assistantMessage.status = '2'
      assistantMessage.errorMessage = typeof data === 'string' ? data : '回复失败'
      this.scrollToBottom()
    },
    stopStream() {
      if (this.streamTask) {
        this.streamTask.abort()
      }
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const el = this.$refs.messageBody
        if (el) {
          el.scrollTop = el.scrollHeight
        }
      })
    }
  }
}
</script>

<style scoped>
.ai-chat-page {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 12px;
  height: calc(100vh - 114px);
  min-height: 560px;
  background: #f3f5f8;
}

.session-panel,
.chat-panel {
  min-height: 0;
  border: 1px solid #dfe4ed;
  border-radius: 4px;
  background: #fff;
}

.session-panel {
  display: flex;
  flex-direction: column;
}

.session-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-bottom: 1px solid #ebeef5;
}

.session-list {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 8px;
}

.session-item {
  position: relative;
  display: block;
  width: 100%;
  min-height: 58px;
  margin-bottom: 6px;
  padding: 9px 34px 8px 10px;
  color: #303133;
  text-align: left;
  background: #fff;
  border: 1px solid transparent;
  border-radius: 4px;
  cursor: pointer;
}

.session-item:hover,
.session-item.active {
  background: #f5f7fb;
  border-color: #cfd8e6;
}

.session-title,
.session-meta {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-title {
  font-size: 13px;
  font-weight: 600;
}

.session-meta {
  margin-top: 7px;
  color: #909399;
  font-size: 12px;
}

.session-delete {
  position: absolute;
  right: 10px;
  top: 20px;
  color: #909399;
}

.session-delete:hover {
  color: #f56c6c;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 62px;
  padding: 0 18px;
  border-bottom: 1px solid #ebeef5;
}

.chat-header h2 {
  margin: 0 0 4px;
  color: #1f2d3d;
  font-size: 16px;
  font-weight: 600;
}

.chat-header span {
  color: #909399;
  font-size: 12px;
}

.message-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 18px;
  background: #f8fafc;
}

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.message-row.is-user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex: 0 0 34px;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  line-height: 34px;
  text-align: center;
  background: #5b7cfa;
}

.message-row.is-user .message-avatar {
  background: #13c2a3;
}

.message-bubble {
  max-width: min(760px, 78%);
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.message-row.is-user .message-bubble {
  background: #ecf8f5;
  border-color: #cceee8;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  color: #606266;
  font-size: 12px;
}

.message-content {
  color: #303133;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-error {
  margin-top: 8px;
  color: #f56c6c;
  font-size: 12px;
}

.typing-caret {
  display: inline-block;
  width: 6px;
  height: 16px;
  margin-left: 2px;
  vertical-align: -3px;
  background: #5b7cfa;
  animation: caret-blink 1s steps(2, start) infinite;
}

.compose-bar {
  padding: 12px 14px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}

.compose-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.input-count {
  color: #909399;
  font-size: 12px;
}

@keyframes caret-blink {
  to {
    visibility: hidden;
  }
}

@media (max-width: 768px) {
  .ai-chat-page {
    grid-template-columns: 1fr;
    height: auto;
    min-height: 0;
  }

  .session-panel {
    height: 230px;
  }

  .chat-panel {
    height: calc(100vh - 360px);
    min-height: 520px;
  }

  .message-bubble {
    max-width: 82%;
  }
}
</style>
