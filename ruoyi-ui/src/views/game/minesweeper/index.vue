<template>
  <div class="app-container minesweeper-page">
    <div class="minesweeper-toolbar">
      <el-radio-group v-model="difficulty" size="small" @change="handleDifficultyChange">
        <el-radio-button label="beginner">初级</el-radio-button>
        <el-radio-button label="intermediate">中级</el-radio-button>
        <el-radio-button label="expert">高级</el-radio-button>
        <el-radio-button label="custom">自定义</el-radio-button>
      </el-radio-group>

      <div class="custom-controls" :class="{ disabled: difficulty !== 'custom' }">
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
      </div>

      <el-button type="primary" size="small" icon="el-icon-refresh" @click="restartGame">重新开始</el-button>
    </div>

    <div class="minesweeper-status">
      <el-tag :type="statusType" effect="dark">{{ statusText }}</el-tag>
      <span class="status-item">时间：{{ formattedTime }}</span>
      <span class="status-item">剩余雷数：{{ game.remainingMines }}</span>
      <span class="status-item">已翻开：{{ revealedText }}</span>
    </div>

    <div class="minesweeper-body">
      <div class="board-scroll">
        <div class="mine-board" :style="boardStyle" @contextmenu.prevent>
          <button
            v-for="cell in flatCells"
            :key="cell.row + '-' + cell.col"
            type="button"
            class="mine-cell"
            :class="cellClass(cell)"
            :title="cellTitle(cell)"
            @click="handleReveal(cell)"
            @contextmenu.prevent="handleFlag(cell)"
            @touchstart.prevent="handleTouchStart(cell)"
            @touchmove.prevent="cancelLongPress"
            @touchend.prevent="handleTouchEnd(cell)"
            @touchcancel.prevent="cancelLongPress"
          >
            <span>{{ cellText(cell) }}</span>
          </button>
        </div>
      </div>

      <div class="round-summary">
        <div class="summary-title">本局</div>
        <div class="summary-row">
          <span>棋盘</span>
          <strong>{{ game.rows }} x {{ game.cols }}</strong>
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
      </div>
    </div>
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
    cellTitle(cell) {
      return '第 ' + (cell.row + 1) + ' 行，第 ' + (cell.col + 1) + ' 列'
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
  --mine-cell-size: 28px;
  color: #27313f;
}

.minesweeper-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.custom-controls {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.custom-controls.disabled {
  opacity: 0.72;
}

.control-label {
  font-size: 13px;
  color: #606266;
}

.minesweeper-status {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  min-height: 32px;
  margin-bottom: 14px;
}

.status-item {
  font-size: 13px;
  color: #4b5565;
  white-space: nowrap;
}

.minesweeper-body {
  display: flex;
  align-items: flex-start;
  gap: 18px;
}

.board-scroll {
  max-width: 100%;
  overflow: auto;
  padding: 2px 2px 10px;
}

.mine-board {
  display: grid;
  gap: 2px;
  padding: 8px;
  width: max-content;
  background: #d5dce5;
  border: 1px solid #b8c3cf;
  border-radius: 6px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.mine-cell {
  width: var(--mine-cell-size);
  height: var(--mine-cell-size);
  padding: 0;
  border: 1px solid #9eabb8;
  border-radius: 4px;
  background: linear-gradient(180deg, #f4f7fb 0%, #c7d1dc 100%);
  color: #1f2d3d;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
  text-align: center;
  cursor: pointer;
  user-select: none;
  touch-action: manipulation;
}

.mine-cell:hover {
  border-color: #409eff;
}

.mine-cell.revealed {
  background: #eef2f6;
  border-color: #c7d0da;
  box-shadow: none;
  cursor: default;
}

.mine-cell.flagged {
  background: #fff3cf;
  border-color: #e6a23c;
  color: #9a5b00;
}

.mine-cell.mine {
  background: #3f4754;
  border-color: #2f3640;
  color: #ffffff;
}

.mine-cell.exploded {
  background: #f56c6c;
  border-color: #d94b4b;
  color: #ffffff;
}

.mine-cell.wrong {
  background: #fbe3e3;
  border-color: #f56c6c;
  color: #c0392b;
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
  min-width: 160px;
  padding: 12px 14px;
  border: 1px solid #d7dee8;
  border-radius: 6px;
  background: #ffffff;
}

.summary-title {
  margin-bottom: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #27313f;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  min-height: 28px;
  font-size: 13px;
  color: #606266;
}

.summary-row strong {
  color: #1f2d3d;
}

@media (max-width: 768px) {
  .minesweeper-page {
    --mine-cell-size: 26px;
  }

  .minesweeper-body {
    display: block;
  }

  .round-summary {
    margin-top: 12px;
    width: 100%;
  }
}
</style>
