const assert = require('assert')
const {
  PLAYERS,
  DIRECTIONS,
  createBoard,
  createGame,
  selectPiece,
  movePiece,
  endTurn,
  getLegalMoves,
  isWinner,
  toKey
} = require('./chineseCheckers')

function run(name, test) {
  try {
    test()
    console.log('[OK] ' + name)
  } catch (error) {
    console.error('[FAIL] ' + name)
    throw error
  }
}

function pieceAt(game, position) {
  return game.pieces[toKey(position)]
}

function hasMove(moves, position, type) {
  return moves.some(move => move.to.key === toKey(position) && (!type || move.type === type))
}

function moveTo(game, from, to) {
  return movePiece(selectPiece(game, from), from, to)
}

run('创建标准星形棋盘', () => {
  const board = createBoard()
  assert.strictEqual(board.points.length, 121)
  assert.strictEqual(board.camps[PLAYERS.PLAYER_ONE].length, 10)
  assert.strictEqual(board.camps[PLAYERS.PLAYER_TWO].length, 10)
})

run('创建双人新局并摆放双方棋子', () => {
  const game = createGame()
  assert.strictEqual(game.status, 'playing')
  assert.strictEqual(game.currentPlayer, PLAYERS.PLAYER_ONE)
  assert.strictEqual(game.moveCount, 0)
  assert.strictEqual(Object.values(game.pieces).filter(player => player === PLAYERS.PLAYER_ONE).length, 10)
  assert.strictEqual(Object.values(game.pieces).filter(player => player === PLAYERS.PLAYER_TWO).length, 10)
  game.board.camps[PLAYERS.PLAYER_ONE].forEach(position => {
    assert.strictEqual(pieceAt(game, position), PLAYERS.PLAYER_ONE)
  })
  game.board.camps[PLAYERS.PLAYER_TWO].forEach(position => {
    assert.strictEqual(pieceAt(game, position), PLAYERS.PLAYER_TWO)
  })
})

run('当前回合只能选择自己的棋子', () => {
  const game = createGame()
  const ownPiece = { q: 0, r: -5 }
  const enemyPiece = game.board.camps[PLAYERS.PLAYER_TWO][0]
  const selected = selectPiece(game, ownPiece)
  assert.deepStrictEqual(selected.selectedPiece, ownPiece)
  assert.ok(selected.legalMoves.length > 0)
  const enemySelected = selectPiece(game, enemyPiece)
  assert.strictEqual(enemySelected.selectedPiece, null)
  const emptySelected = selectPiece(game, { q: 0, r: 0 })
  assert.strictEqual(emptySelected.selectedPiece, null)
})

run('相邻空位可一步移动并切换回合', () => {
  const game = createGame()
  const from = { q: 0, r: -5 }
  const to = { q: 0, r: -4 }
  const selected = selectPiece(game, from)
  assert.ok(hasMove(selected.legalMoves, to, 'step'))
  const next = movePiece(selected, from, to)
  assert.strictEqual(pieceAt(next, from), undefined)
  assert.strictEqual(pieceAt(next, to), PLAYERS.PLAYER_ONE)
  assert.strictEqual(next.currentPlayer, PLAYERS.PLAYER_TWO)
  assert.strictEqual(next.moveCount, 1)
})

run('被占用落点和棋盘外目标不能移动', () => {
  const game = createGame()
  const from = game.board.camps[PLAYERS.PLAYER_ONE][0]
  const occupied = game.board.camps[PLAYERS.PLAYER_ONE][1]
  const selected = selectPiece(game, from)
  const unchangedOccupied = movePiece(selected, from, occupied)
  const unchangedOutside = movePiece(selected, from, { q: 20, r: 20 })
  assert.deepStrictEqual(unchangedOccupied, selected)
  assert.deepStrictEqual(unchangedOutside, selected)
})

run('有中间棋子且落点为空时可跳跃', () => {
  const game = createGame({
    pieces: {
      '0,-2': PLAYERS.PLAYER_ONE,
      '0,-1': PLAYERS.PLAYER_TWO
    }
  })
  const from = { q: 0, r: -2 }
  const middle = { q: 0, r: -1 }
  const to = { q: 0, r: 0 }
  assert.ok(pieceAt(game, middle))
  assert.strictEqual(pieceAt(game, to), undefined)
  const selected = selectPiece(game, from)
  assert.ok(hasMove(selected.legalMoves, to, 'jump'))
  const jumped = movePiece(selected, from, to)
  assert.strictEqual(pieceAt(jumped, from), undefined)
  assert.strictEqual(pieceAt(jumped, middle), PLAYERS.PLAYER_TWO)
  assert.strictEqual(pieceAt(jumped, to), PLAYERS.PLAYER_ONE)
})

run('没有中间棋子不能跳跃', () => {
  const game = createGame({
    pieces: {
      '0,-2': PLAYERS.PLAYER_ONE
    }
  })
  const moves = getLegalMoves(game, { q: 0, r: -2 })
  assert.strictEqual(hasMove(moves, { q: 0, r: 0 }, 'jump'), false)
})

run('跳跃后如仍可继续跳跃则保持同一棋子', () => {
  let game = createGame({
    pieces: {
      '0,-2': PLAYERS.PLAYER_ONE,
      '0,-1': PLAYERS.PLAYER_TWO,
      '0,1': PLAYERS.PLAYER_TWO
    }
  })
  game = selectPiece(game, { q: 0, r: -2 })
  game = movePiece(game, { q: 0, r: -2 }, { q: 0, r: 0 })
  assert.strictEqual(game.currentPlayer, PLAYERS.PLAYER_ONE)
  assert.deepStrictEqual(game.selectedPiece, { q: 0, r: 0 })
  assert.strictEqual(game.chainJump, true)
  assert.ok(hasMove(game.legalMoves, { q: 0, r: 2 }, 'jump'))

  const attemptedStep = movePiece(game, { q: 0, r: 0 }, DIRECTIONS.map(direction => ({
    q: direction.q,
    r: direction.r
  })).find(position => game.board.pointMap[toKey(position)] && !pieceAt(game, position)))
  assert.deepStrictEqual(attemptedStep, game)

  const attemptedSelect = selectPiece(game, { q: 1, r: -1 })
  assert.deepStrictEqual(attemptedSelect, game)
})

run('连续跳跃可主动结束回合', () => {
  let game = createGame({
    pieces: {
      '0,-2': PLAYERS.PLAYER_ONE,
      '0,-1': PLAYERS.PLAYER_TWO,
      '0,1': PLAYERS.PLAYER_TWO
    }
  })
  game = movePiece(selectPiece(game, { q: 0, r: -2 }), { q: 0, r: -2 }, { q: 0, r: 0 })
  const next = endTurn(game)
  assert.strictEqual(next.currentPlayer, PLAYERS.PLAYER_TWO)
  assert.strictEqual(next.chainJump, false)
  assert.strictEqual(next.selectedPiece, null)
  assert.strictEqual(next.moveCount, 1)
})

run('无后续跳跃时自动结束回合', () => {
  let game = createGame({
    pieces: {
      '0,-2': PLAYERS.PLAYER_ONE,
      '0,-1': PLAYERS.PLAYER_TWO
    }
  })
  game = movePiece(selectPiece(game, { q: 0, r: -2 }), { q: 0, r: -2 }, { q: 0, r: 0 })
  assert.strictEqual(game.currentPlayer, PLAYERS.PLAYER_TWO)
  assert.strictEqual(game.chainJump, false)
  assert.strictEqual(game.moveCount, 1)
})

run('玩家全部进入目标营地后获胜', () => {
  const base = createGame()
  const playerOnePieces = {}
  base.board.camps[PLAYERS.PLAYER_TWO].forEach(position => {
    playerOnePieces[toKey(position)] = PLAYERS.PLAYER_ONE
  })
  const won = createGame({ pieces: playerOnePieces })
  assert.strictEqual(isWinner(won, PLAYERS.PLAYER_ONE), true)

  const afterWin = movePiece(won, won.board.camps[PLAYERS.PLAYER_TWO][0], { q: 0, r: 0 })
  assert.deepStrictEqual(afterWin, won)
})
