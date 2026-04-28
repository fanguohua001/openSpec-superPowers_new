<template>
  <div class="app-container minesweeper-page">
    <section class="game-shell" :class="'state-' + game.status">
      <header class="game-header">
        <div class="title-block">
          <div class="eyebrow">休闲游戏</div>
          <h2>扫雷</h2>
        </div>
        <el-button type="primary" size="small" icon="el-icon-refresh" @click="restartGame">重新开始</el-button>
      </header>

      <div class="control-bar">
        <el-radio-group v-model="difficulty" size="small" class="difficulty-tabs" @change="handleDifficultyChange">
          <el-radio-button label="beginner">初级</el-radio-button>
          <el-radio-button label="intermediate">中级</el-radio-button>
          <el-radio-button label="expert">高级</el-radio-button>
          <el-radio-button label="custom">自定义</el-radio-button>
        </el-radio-group>

        <div class="custom-controls" :class="{ disabled: difficulty !== 'custom' }">
          <label class="control-field">
            <span class="control-label">行</span>
            <el-input-number
              v-model="customForm.rows"
              size="small"
              controls-position="right"
              :min="5"
              :max="24"
              :disabled="difficulty !== 'custom'"
              @change="handleCustomChange"
            />
          </label>
          <label class="control-field">
            <span class="control-label">列</span>
            <el-input-number
              v-model="customForm.cols"
              size="small"
              controls-position="right"
              :min="5"
              :max="40"
              :disabled="difficulty !== 'custom'"
              @change="handleCustomChange"
            />
          </label>
          <label class="control-field">
            <span class="control-label">雷</span>
            <el-input-number
              v-model="customForm.mines"
              size="small"
              controls-position="right"
              :min="1"
              :max="customMaxMines"
              :disabled="difficulty !== 'custom'"
              @change="handleCustomChange"
            />
          </label>
        </div>
      </div>

      <div class="stat-strip" aria-live="polite">
        <div class="stat-tile status-tile">
          <span class="stat-label">状态</span>
          <strong>{{ statusText }}</strong>
        </div>
        <div class="stat-tile">
          <span class="stat-label">时间</span>
          <strong>{{ formattedTime }}</strong>
        </div>
        <div class="stat-tile">
          <span class="stat-label">剩余雷数</span>
          <strong>{{ game.remainingMines }}</strong>
        </div>
        <div class="stat-tile">
          <span class="stat-label">已翻开</span>
          <strong>{{ revealedText }}</strong>
        </div>
      </div>

      <div class="minesweeper-body">
        <div class="board-panel">
          <div class="board-header">
            <div>
              <span class="panel-label">棋盘</span>
              <strong>{{ game.rows }} x {{ game.cols }}</strong>
            </div>
            <el-tag :type="statusType" size="mini">{{ difficultyText }}</el-tag>
          </div>
          <transition name="result-fade">
            <div v-if="isFinished" class="result-banner" :class="'result-' + game.status">
              <div>
                <strong>{{ resultTitle }}</strong>
                <span>{{ resultText }}</span>
              </div>
              <el-button size="mini" icon="el-icon-refresh" @click="restartGame">再来一局</el-button>
            </div>
          </transition>
          <div class="board-scroll">
            <div class="mine-board" :style="boardStyle" :aria-label="boardAriaLabel" @contextmenu.prevent>
              <button
                v-for="cell in flatCells"
                :key="cell.row + '-' + cell.col"
                type="button"
                class="mine-cell"
                :class="cellClass(cell)"
                :title="cellTitle(cell)"
                :aria-label="cellAriaLabel(cell)"
                @click="handleReveal(cell)"
                @keydown="handleCellKeydown($event, cell)"
                @contextmenu.prevent="handleFlag(cell)"
                @touchstart.prevent="handleTouchStart(cell)"
                @touchmove.prevent="cancelLongPress"
                @touchend.prevent="handleTouchEnd(cell)"
                @touchcancel.prevent="cancelLongPress"
              >
                <i v-if="cellIcon(cell)" :class="cellIcon(cell)" aria-hidden="true"></i>
                <span v-else>{{ cellText(cell) }}</span>
              </button>
            </div>
          </div>
        </div>

        <aside class="round-summary">
          <div class="summary-title">本局</div>
          <div class="progress-block">
            <div class="progress-meta">
              <span>安全进度</span>
              <strong>{{ revealProgress }}%</strong>
            </div>
            <div class="progress-track">
              <span :style="{ width: revealProgress + '%' }"></span>
            </div>
          </div>
          <div class="summary-row">
            <span>难度</span>
            <strong>{{ difficultyText }}</strong>
          </div>
          <div class="summary-row">
            <span>地雷</span>
            <strong>{{ game.mines }}</strong>
          </div>
          <div class="summary-row">
            <span>旗标</span>
            <strong>{{ game.flagCount }}</strong>
          </div>
          <div class="summary-row">
            <span>安全格</span>
            <strong>{{ safeCellCount }}</strong>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<script>
import { DIFFICULTIES, createGame, revealCell, toggleFlag } from './minesweeper'

export default {
  name: 'MinesweeperGame',
  data() {
    return {
      difficulty: 'beginner',
      customForm: {
        rows: 9,
        cols: 9,
        mines: 10
      },
      game: createGame({ difficulty: 'beginner' }),
      elapsedSeconds: 0,
      timer: null,
      longPressTimer: null,
      longPressTriggered: false,
      touchCell: null
    }
  },
  computed: {
    flatCells() {
      return this.game.cells.reduce((items, row) => items.concat(row), [])
    },
    boardStyle() {
      return {
        gridTemplateColumns: 'repeat(' + this.game.cols + ', var(--mine-cell-size))'
      }
    },
    customMaxMines() {
      const rows = Number(this.customForm.rows) || 5
      const cols = Number(this.customForm.cols) || 5
      return Math.max(1, rows * cols - 9)
    },
    safeCellCount() {
      return this.game.rows * this.game.cols - this.game.mines
    },
    revealedText() {
      return this.game.revealedCount + ' / ' + this.safeCellCount
    },
    revealProgress() {
      if (this.safeCellCount <= 0) {
        return 0
      }
      return Math.min(100, Math.round((this.game.revealedCount / this.safeCellCount) * 100))
    },
    difficultyText() {
      const textMap = {
        beginner: '初级',
        intermediate: '中级',
        expert: '高级',
        custom: '自定义'
      }
      return textMap[this.difficulty] || '自定义'
    },
    boardAriaLabel() {
      return '扫雷棋盘，' + this.game.rows + ' 行，' + this.game.cols + ' 列'
    },
    isFinished() {
      return this.game.status === 'won' || this.game.status === 'lost'
    },
    resultTitle() {
      return this.game.status === 'won' ? '胜利' : '失败'
    },
    resultText() {
      if (this.game.status === 'won') {
        return '全部安全格已翻开，用时 ' + this.formattedTime
      }
      return '踩中地雷，本局用时 ' + this.formattedTime
    },
    formattedTime() {
      const minutes = Math.floor(this.elapsedSeconds / 60)
      const seconds = this.elapsedSeconds % 60
      return this.padTime(minutes) + ':' + this.padTime(seconds)
    },
    statusText() {
      const textMap = {
        ready: '未开始',
        playing: '进行中',
        won: '已胜利',
        lost: '已失败'
      }
      return textMap[this.game.status] || '未开始'
    },
    statusType() {
      const typeMap = {
        ready: 'info',
        playing: '',
        won: 'success',
        lost: 'danger'
      }
      return typeMap[this.game.status] || 'info'
    }
  },
  beforeDestroy() {
    this.stopTimer()
    this.cancelLongPress()
  },
  methods: {
    handleDifficultyChange(value) {
      if (value !== 'custom' && DIFFICULTIES[value]) {
        this.customForm = Object.assign({}, DIFFICULTIES[value])
      }
      this.restartGame()
    },
    handleCustomChange() {
      if (this.difficulty !== 'custom') {
        return
      }
      if (this.customForm.mines > this.customMaxMines) {
        this.customForm.mines = this.customMaxMines
      }
      this.restartGame()
    },
    restartGame() {
      const options = this.difficulty === 'custom'
        ? Object.assign({ difficulty: 'custom' }, this.customForm)
        : { difficulty: this.difficulty }
      this.game = createGame(options)
      this.elapsedSeconds = 0
      this.stopTimer()
      this.cancelLongPress()
    },
    handleReveal(cell) {
      this.applyGame(revealCell(this.game, cell.row, cell.col))
    },
    handleFlag(cell) {
      this.applyGame(toggleFlag(this.game, cell.row, cell.col))
    },
    handleCellKeydown(event, cell) {
      if (event.key === 'f' || event.key === 'F') {
        event.preventDefault()
        this.handleFlag(cell)
      }
    },
    applyGame(nextGame) {
      const wasStarted = this.game.started
      this.game = nextGame
      if (!wasStarted && this.game.started && this.game.status === 'playing') {
        this.startTimer()
      }
      if (this.game.status === 'won' || this.game.status === 'lost') {
        this.stopTimer()
      }
    },
    startTimer() {
      if (this.timer) {
        return
      }
      this.timer = window.setInterval(() => {
        this.elapsedSeconds += 1
      }, 1000)
    },
    stopTimer() {
      if (this.timer) {
        window.clearInterval(this.timer)
        this.timer = null
      }
    },
    handleTouchStart(cell) {
      this.cancelLongPress()
      this.longPressTriggered = false
      this.touchCell = cell
      this.longPressTimer = window.setTimeout(() => {
        this.longPressTriggered = true
        this.handleFlag(cell)
      }, 450)
    },
    handleTouchEnd(cell) {
      const shouldReveal = !this.longPressTriggered && this.touchCell === cell
      this.cancelLongPress()
      if (shouldReveal) {
        this.handleReveal(cell)
      }
    },
    cancelLongPress() {
      if (this.longPressTimer) {
        window.clearTimeout(this.longPressTimer)
        this.longPressTimer = null
      }
      this.touchCell = null
    },
    cellClass(cell) {
      const classMap = {
        revealed: cell.revealed,
        flagged: this.isFlagVisible(cell),
        mine: this.game.status === 'lost' && cell.mine,
        exploded: cell.exploded,
        wrong: cell.wrongFlag
      }
      if (cell.revealed && cell.adjacentMines > 0) {
        classMap['number-' + cell.adjacentMines] = true
      }
      return classMap
    },
    cellText(cell) {
      if (this.game.status === 'lost' && cell.wrongFlag) {
        return 'X'
      }
      if (this.game.status === 'lost' && cell.mine) {
        return '*'
      }
      if (this.isFlagVisible(cell)) {
        return 'F'
      }
      if (!cell.revealed) {
        return ''
      }
      return cell.adjacentMines > 0 ? String(cell.adjacentMines) : ''
    },
    cellIcon(cell) {
      if (this.game.status === 'lost' && cell.wrongFlag) {
        return 'el-icon-close'
      }
      if (this.game.status === 'lost' && cell.mine) {
        return 'el-icon-warning'
      }
      if (this.isFlagVisible(cell)) {
        return 'el-icon-s-flag'
      }
      return ''
    },
    cellTitle(cell) {
      return '第 ' + (cell.row + 1) + ' 行，第 ' + (cell.col + 1) + ' 列'
    },
    cellAriaLabel(cell) {
      const position = '第 ' + (cell.row + 1) + ' 行，第 ' + (cell.col + 1) + ' 列'
      if (this.game.status === 'lost' && cell.wrongFlag) {
        return position + '，错误旗标'
      }
      if (this.game.status === 'lost' && cell.mine) {
        return position + '，地雷'
      }
      if (this.isFlagVisible(cell)) {
        return position + '，已标旗'
      }
      if (!cell.revealed) {
        return position + '，未翻开'
      }
      if (cell.adjacentMines > 0) {
        return position + '，数字 ' + cell.adjacentMines
      }
      return position + '，空白'
    },
    isFlagVisible(cell) {
      return cell.flagged && !cell.wrongFlag && !(this.game.status === 'lost' && cell.mine)
    },
    padTime(value) {
      return value < 10 ? '0' + value : String(value)
    }
  }
}
</script>

<style scoped>
.minesweeper-page {
  --mine-cell-size: 32px;
  color: #1f2d3d;
}

.game-shell {
  min-height: calc(100vh - 132px);
  padding: 18px;
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(247, 250, 252, 0.98)),
    #f7fafc;
}

.game-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
}

.title-block {
  min-width: 180px;
}

.eyebrow {
  margin-bottom: 4px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.title-block h2 {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.25;
}

.control-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px 16px;
  margin-bottom: 14px;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
}

.custom-controls {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.custom-controls.disabled {
  opacity: 0.72;
}

.control-field {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 36px;
}

.control-label {
  font-size: 13px;
  font-weight: 600;
  color: #606266;
}

.control-field .el-input-number {
  width: 96px;
}

.stat-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(128px, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.stat-tile {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  min-height: 54px;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
}

.stat-label {
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.stat-tile strong {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.status-tile {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.state-won .status-tile {
  border-color: #bbf7d0;
  background: #ecfdf5;
}

.state-lost .status-tile {
  border-color: #fecaca;
  background: #fef2f2;
}

.minesweeper-body {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.board-panel {
  flex: 1 1 auto;
  min-width: 0;
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
}

.board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  min-height: 48px;
  padding: 10px 12px;
  border-bottom: 1px solid #e5edf5;
}

.panel-label {
  margin-right: 8px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.board-header strong {
  color: #0f172a;
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}

.result-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  min-height: 52px;
  padding: 10px 12px;
  border-bottom: 1px solid #e5edf5;
}

.result-banner strong {
  display: block;
  margin-bottom: 2px;
  color: #0f172a;
  font-size: 15px;
  line-height: 1.35;
}

.result-banner span {
  color: #475569;
  font-size: 13px;
  line-height: 1.4;
}

.result-won {
  background: #ecfdf5;
}

.result-lost {
  background: #fef2f2;
}

.result-fade-enter-active,
.result-fade-leave-active {
  transition: opacity 160ms ease, transform 160ms ease;
}

.result-fade-enter,
.result-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

.board-scroll {
  max-width: 100%;
  overflow: auto;
  padding: 14px;
  scrollbar-color: #94a3b8 #e2e8f0;
}

.mine-board {
  display: grid;
  gap: 3px;
  padding: 10px;
  width: max-content;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background: #dbe3ee;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.84);
}

.mine-cell {
  width: var(--mine-cell-size);
  height: var(--mine-cell-size);
  min-width: var(--mine-cell-size);
  padding: 0;
  border: 1px solid #94a3b8;
  border-radius: 6px;
  background: linear-gradient(180deg, #ffffff 0%, #d7e0ea 100%);
  color: #0f172a;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
  text-align: center;
  cursor: pointer;
  user-select: none;
  touch-action: manipulation;
  transition: background-color 160ms ease, border-color 160ms ease, box-shadow 160ms ease, transform 120ms ease;
}

.mine-cell span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-variant-numeric: tabular-nums;
}

.mine-cell i {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  font-size: 15px;
  line-height: 1;
}

.mine-cell:hover {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.14);
}

.mine-cell:active {
  transform: translateY(1px);
}

.mine-cell:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.24);
}

.mine-cell.revealed {
  background: #f8fafc;
  border-color: #d7dee8;
  box-shadow: none;
  cursor: default;
}

.mine-cell.flagged {
  background: #fff7ed;
  border-color: #f59e0b;
  color: #92400e;
  box-shadow: inset 0 0 0 1px rgba(245, 158, 11, 0.2);
}

.mine-cell.mine {
  background: #263241;
  border-color: #111827;
  color: #ffffff;
}

.mine-cell.exploded {
  background: #dc2626;
  border-color: #991b1b;
  color: #ffffff;
}

.mine-cell.wrong {
  background: #fff1f2;
  border-color: #fb7185;
  color: #be123c;
}

.mine-cell.number-1 {
  color: #2563eb;
}

.mine-cell.number-2 {
  color: #059669;
}

.mine-cell.number-3 {
  color: #dc2626;
}

.mine-cell.number-4 {
  color: #7c3aed;
}

.mine-cell.number-5 {
  color: #b45309;
}

.mine-cell.number-6 {
  color: #0891b2;
}

.mine-cell.number-7 {
  color: #111827;
}

.mine-cell.number-8 {
  color: #64748b;
}

.round-summary {
  flex: 0 0 220px;
  padding: 14px;
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background: #ffffff;
}

.summary-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}

.progress-block {
  margin-bottom: 12px;
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
}

.progress-meta strong {
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.progress-track {
  height: 8px;
  overflow: hidden;
  border-radius: 8px;
  background: #e2e8f0;
}

.progress-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2563eb, #14b8a6);
  transition: width 180ms ease;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  min-height: 34px;
  border-top: 1px solid #edf2f7;
  font-size: 13px;
  color: #64748b;
}

.summary-row strong {
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

@media (prefers-reduced-motion: reduce) {
  .mine-cell,
  .progress-track span,
  .result-fade-enter-active,
  .result-fade-leave-active {
    transition: none;
  }
}

@media (max-width: 768px) {
  .minesweeper-page {
    --mine-cell-size: 40px;
  }

  .game-shell {
    min-height: auto;
    padding: 12px;
  }

  .title-block h2 {
    font-size: 22px;
  }

  .control-bar {
    display: block;
  }

  .difficulty-tabs {
    margin-bottom: 10px;
  }

  .custom-controls {
    gap: 8px 10px;
  }

  .stat-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .minesweeper-body {
    display: block;
  }

  .board-scroll {
    padding: 10px;
  }

  .round-summary {
    margin-top: 12px;
    width: 100%;
  }
}

@media (max-width: 480px) {
  .minesweeper-page {
    --mine-cell-size: 44px;
  }

  .game-header {
    align-items: stretch;
  }

  .game-header .el-button {
    width: 100%;
  }

  .stat-strip {
    grid-template-columns: 1fr;
  }

  .control-field {
    width: 100%;
    justify-content: space-between;
  }

  .result-banner {
    align-items: stretch;
    flex-direction: column;
  }

  .result-banner .el-button {
    width: 100%;
  }
}
</style>
