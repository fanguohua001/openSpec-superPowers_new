<template>
  <div class="app-container chinese-checkers-page">
    <section class="game-shell" :class="{ finished: game.status === 'finished' }">
      <header class="game-header">
        <div class="title-block">
          <div class="eyebrow">休闲游戏</div>
          <h2>跳棋</h2>
        </div>
        <div class="header-actions">
          <el-button
            v-if="game.chainJump"
            type="warning"
            size="small"
            icon="el-icon-check"
            @click="handleEndTurn"
          >
            结束回合
          </el-button>
          <el-button type="primary" size="small" icon="el-icon-refresh" @click="restartGame">重新开始</el-button>
        </div>
      </header>

      <div class="status-strip" aria-live="polite">
        <div class="status-tile current-player" :class="currentPlayerClass">
          <span class="status-label">当前回合</span>
          <strong>{{ currentPlayerName }}</strong>
        </div>
        <div class="status-tile">
          <span class="status-label">步数</span>
          <strong>{{ game.moveCount }}</strong>
        </div>
        <div class="status-tile">
          <span class="status-label">状态</span>
          <strong>{{ statusText }}</strong>
        </div>
        <div class="status-tile">
          <span class="status-label">可走点</span>
          <strong>{{ game.legalMoves.length }}</strong>
        </div>
      </div>

      <div class="checkers-body">
        <div class="board-panel">
          <div class="board-header">
            <div>
              <span class="panel-label">棋盘</span>
              <strong>121 个落点</strong>
            </div>
            <el-tag :type="game.chainJump ? 'warning' : statusType" size="mini">{{ turnHint }}</el-tag>
          </div>

          <transition name="result-fade">
            <div v-if="game.status === 'finished'" class="result-banner">
              <div>
                <strong>{{ winnerName }} 获胜</strong>
                <span>所有棋子已经进入对面营地，本局共 {{ game.moveCount }} 步。</span>
              </div>
              <el-button size="mini" icon="el-icon-refresh" @click="restartGame">再来一局</el-button>
            </div>
          </transition>

          <div class="board-scroll">
            <div class="star-board" :style="boardStyle" aria-label="中国跳棋棋盘">
              <button
                v-for="point in boardPoints"
                :key="point.key"
                type="button"
                class="board-point"
                :class="pointClass(point)"
                :style="pointStyle(point)"
                :title="pointTitle(point)"
                :aria-label="pointAriaLabel(point)"
                @click="handlePointClick(point)"
              >
                <span v-if="pieceAt(point)" class="piece" :class="'piece-' + pieceAt(point)"></span>
                <span v-else-if="moveAt(point)" class="move-dot" :class="'move-' + moveAt(point).type"></span>
              </button>
            </div>
          </div>
        </div>

        <aside class="round-summary">
          <div class="summary-title">本局</div>
          <div class="player-card player-one" :class="{ active: game.currentPlayer === PLAYERS.PLAYER_ONE }">
            <div class="player-head">
              <span class="player-mark"></span>
              <strong>玩家一</strong>
            </div>
            <div class="summary-row">
              <span>目标剩余</span>
              <strong>{{ remainingTargetCount(PLAYERS.PLAYER_ONE) }}</strong>
            </div>
          </div>
          <div class="player-card player-two" :class="{ active: game.currentPlayer === PLAYERS.PLAYER_TWO }">
            <div class="player-head">
              <span class="player-mark"></span>
              <strong>玩家二</strong>
            </div>
            <div class="summary-row">
              <span>目标剩余</span>
              <strong>{{ remainingTargetCount(PLAYERS.PLAYER_TWO) }}</strong>
            </div>
          </div>
          <div class="tip-block">
            <strong>{{ turnHint }}</strong>
            <span>{{ helperText }}</span>
          </div>
        </aside>
      </div>
    </section>
  </div>
</template>

<script>
import {
  PLAYERS,
  createGame,
  selectPiece,
  movePiece,
  endTurn,
  toKey
} from './chineseCheckers'

export default {
  name: 'ChineseCheckersGame',
  data() {
    return {
      PLAYERS,
      game: createGame()
    }
  },
  computed: {
    boardPoints() {
      const rawPoints = this.game.board.points.map(point => {
        return Object.assign({}, point, {
          x: point.q + point.r / 2,
          y: point.r * 0.866
        })
      })
      const xs = rawPoints.map(point => point.x)
      const ys = rawPoints.map(point => point.y)
      const minX = Math.min.apply(null, xs)
      const minY = Math.min.apply(null, ys)
      const maxX = Math.max.apply(null, xs)
      const maxY = Math.max.apply(null, ys)
      return rawPoints.map(point => {
        return Object.assign({}, point, {
          left: ((point.x - minX) / (maxX - minX)) * 100,
          top: ((point.y - minY) / (maxY - minY)) * 100
        })
      })
    },
    boardStyle() {
      return {
        '--point-size': '34px'
      }
    },
    currentPlayerName() {
      return this.playerName(this.game.currentPlayer)
    },
    currentPlayerClass() {
      return this.game.currentPlayer === PLAYERS.PLAYER_ONE ? 'player-one' : 'player-two'
    },
    winnerName() {
      return this.playerName(this.game.winner)
    },
    statusText() {
      if (this.game.status === 'finished') {
        return '已结束'
      }
      return this.game.chainJump ? '连续跳跃' : '进行中'
    },
    statusType() {
      return this.game.status === 'finished' ? 'success' : ''
    },
    turnHint() {
      if (this.game.status === 'finished') {
        return this.winnerName + '获胜'
      }
      if (this.game.chainJump) {
        return '可继续跳跃'
      }
      if (this.game.selectedPiece) {
        return '选择落点'
      }
      return '选择棋子'
    },
    helperText() {
      if (this.game.status === 'finished') {
        return '点击重新开始可以开启新局。'
      }
      if (this.game.chainJump) {
        return '继续点击高亮跳点，或点击顶部的结束回合。'
      }
      if (this.game.selectedPiece) {
        return '实心高亮为可走落点，圆点颜色区分一步和跳跃。'
      }
      return '只能移动当前回合玩家的棋子。'
    }
  },
  methods: {
    restartGame() {
      this.game = createGame()
    },
    handlePointClick(point) {
      if (this.game.status === 'finished') {
        return
      }

      const move = this.moveAt(point)
      if (this.game.selectedPiece && move) {
        this.game = movePiece(this.game, this.game.selectedPiece, point)
        return
      }

      if (this.pieceAt(point) === this.game.currentPlayer) {
        this.game = selectPiece(this.game, point)
      }
    },
    handleEndTurn() {
      this.game = endTurn(this.game)
    },
    pieceAt(point) {
      return this.game.pieces[toKey(point)]
    },
    moveAt(point) {
      const key = toKey(point)
      return this.game.legalMoves.find(move => toKey(move.to) === key)
    },
    pointClass(point) {
      const piece = this.pieceAt(point)
      const move = this.moveAt(point)
      return {
        selected: this.game.selectedPiece && toKey(this.game.selectedPiece) === toKey(point),
        legal: Boolean(move),
        'legal-step': move && move.type === 'step',
        'legal-jump': move && move.type === 'jump',
        occupied: Boolean(piece),
        'camp-one': this.isCamp(point, PLAYERS.PLAYER_ONE),
        'camp-two': this.isCamp(point, PLAYERS.PLAYER_TWO)
      }
    },
    pointStyle(point) {
      return {
        left: point.left + '%',
        top: point.top + '%'
      }
    },
    pointTitle(point) {
      return '坐标 ' + point.q + ',' + point.r
    },
    pointAriaLabel(point) {
      const piece = this.pieceAt(point)
      const move = this.moveAt(point)
      if (piece) {
        return this.pointTitle(point) + '，' + this.playerName(piece) + '棋子'
      }
      if (move) {
        return this.pointTitle(point) + '，可' + (move.type === 'jump' ? '跳跃' : '一步移动')
      }
      return this.pointTitle(point) + '，空落点'
    },
    remainingTargetCount(player) {
      const targetKeys = this.game.board.targets[player].map(toKey)
      const occupiedCount = targetKeys.filter(key => this.game.pieces[key] === player).length
      return 10 - occupiedCount
    },
    isCamp(point, player) {
      const key = toKey(point)
      return this.game.board.camps[player].some(campPoint => toKey(campPoint) === key)
    },
    playerName(player) {
      const nameMap = {
        [PLAYERS.PLAYER_ONE]: '玩家一',
        [PLAYERS.PLAYER_TWO]: '玩家二'
      }
      return nameMap[player] || '玩家'
    }
  }
}
</script>

<style scoped>
.chinese-checkers-page {
  color: #1f2933;
}

.game-shell {
  min-height: calc(100vh - 132px);
  padding: 18px;
  border: 1px solid #d9e2ec;
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(247, 250, 252, 0.98)),
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

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.status-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(128px, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.status-tile {
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

.status-label {
  color: #64748b;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.status-tile strong {
  color: #0f172a;
  font-size: 20px;
  font-weight: 700;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.current-player.player-one {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.current-player.player-two {
  border-color: #fecaca;
  background: #fef2f2;
}

.checkers-body {
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
}

.result-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  min-height: 52px;
  padding: 10px 12px;
  border-bottom: 1px solid #bbf7d0;
  background: #ecfdf5;
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
}

.star-board {
  position: relative;
  width: min(760px, calc(100vw - 380px));
  min-width: 560px;
  aspect-ratio: 1 / 0.9;
  margin: 0 auto;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(241, 245, 249, 0.92), rgba(255, 255, 255, 0.96)),
    #f8fafc;
}

.board-point {
  position: absolute;
  width: var(--point-size);
  height: var(--point-size);
  padding: 0;
  border: 1px solid #cbd5e1;
  border-radius: 50%;
  background: #ffffff;
  transform: translate(-50%, -50%);
  cursor: pointer;
  transition: border-color 160ms ease, box-shadow 160ms ease, transform 120ms ease, background-color 160ms ease;
}

.board-point:hover {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.14);
}

.board-point:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.24);
}

.board-point:active {
  transform: translate(-50%, -50%) scale(0.96);
}

.board-point.camp-one {
  background: #eff6ff;
}

.board-point.camp-two {
  background: #fff1f2;
}

.board-point.legal {
  border-color: #16a34a;
  background: #ecfdf5;
  box-shadow: 0 0 0 3px rgba(22, 163, 74, 0.16);
}

.board-point.legal-jump {
  border-color: #f59e0b;
  background: #fff7ed;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.18);
}

.board-point.selected {
  border-color: #111827;
  box-shadow: 0 0 0 4px rgba(17, 24, 39, 0.2);
}

.piece,
.move-dot {
  display: block;
  width: 22px;
  height: 22px;
  margin: 5px auto;
  border-radius: 50%;
}

.piece-playerOne {
  background: linear-gradient(180deg, #60a5fa, #1d4ed8);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5), 0 2px 4px rgba(30, 64, 175, 0.28);
}

.piece-playerTwo {
  background: linear-gradient(180deg, #fb7185, #be123c);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.45), 0 2px 4px rgba(159, 18, 57, 0.28);
}

.move-dot {
  width: 10px;
  height: 10px;
  margin-top: 11px;
  background: #16a34a;
}

.move-jump {
  background: #f59e0b;
}

.round-summary {
  flex: 0 0 240px;
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

.player-card {
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #ffffff;
}

.player-card + .player-card {
  margin-top: 10px;
}

.player-card.active {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.player-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.player-head strong {
  color: #0f172a;
  font-size: 14px;
}

.player-mark {
  width: 14px;
  height: 14px;
  border-radius: 50%;
}

.player-one .player-mark {
  background: #2563eb;
}

.player-two .player-mark {
  background: #e11d48;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  min-height: 28px;
  font-size: 13px;
  color: #64748b;
}

.summary-row strong {
  color: #0f172a;
  font-variant-numeric: tabular-nums;
}

.tip-block {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #edf2f7;
}

.tip-block strong {
  display: block;
  margin-bottom: 4px;
  color: #0f172a;
  font-size: 14px;
}

.tip-block span {
  color: #64748b;
  font-size: 13px;
  line-height: 1.5;
}

@media (prefers-reduced-motion: reduce) {
  .board-point,
  .result-fade-enter-active,
  .result-fade-leave-active {
    transition: none;
  }
}

@media (max-width: 1024px) {
  .checkers-body {
    display: block;
  }

  .star-board {
    width: 680px;
    min-width: 680px;
  }

  .round-summary {
    width: 100%;
    margin-top: 12px;
  }
}

@media (max-width: 768px) {
  .game-shell {
    min-height: auto;
    padding: 12px;
  }

  .title-block h2 {
    font-size: 22px;
  }

  .status-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .board-scroll {
    padding: 10px;
  }

  .star-board {
    --point-size: 40px;
    width: 720px;
    min-width: 720px;
  }
}

@media (max-width: 480px) {
  .game-header {
    align-items: stretch;
  }

  .header-actions,
  .header-actions .el-button {
    width: 100%;
  }

  .status-strip {
    grid-template-columns: 1fr;
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
