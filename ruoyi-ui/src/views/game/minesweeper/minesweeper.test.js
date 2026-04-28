const assert = require('assert')
const {
  createGame,
  buildBoard,
  revealCell,
  toggleFlag,
  getNeighbors
} = require('./minesweeper')

function cell(game, row, col) {
  return game.cells[row][col]
}

function countMines(game) {
  return game.cells.reduce((total, row) => total + row.filter(item => item.mine).length, 0)
}

function run(name, test) {
  try {
    test()
    console.log('[OK] ' + name)
  } catch (error) {
    console.error('[FAIL] ' + name)
    throw error
  }
}

run('创建初级新局', () => {
  const game = createGame({ difficulty: 'beginner' })
  assert.strictEqual(game.rows, 9)
  assert.strictEqual(game.cols, 9)
  assert.strictEqual(game.mines, 10)
  assert.strictEqual(game.status, 'ready')
  assert.strictEqual(game.revealedCount, 0)
  assert.strictEqual(game.flagCount, 0)
  assert.strictEqual(game.boardReady, false)
})

run('限制自定义参数范围', () => {
  const game = createGame({ rows: 2, cols: 100, mines: 999 })
  assert.strictEqual(game.rows, 5)
  assert.strictEqual(game.cols, 40)
  assert.strictEqual(game.mines, 191)
})

run('相邻格计算不越界', () => {
  assert.deepStrictEqual(getNeighbors(0, 0, 5, 5), [
    [0, 1],
    [1, 0],
    [1, 1]
  ])
})

run('首次翻开不会踩雷且安全区不布雷', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 3 })
  const next = revealCell(game, 2, 2, {
    minePositions: [[0, 0], [4, 4], [0, 4]]
  })
  assert.strictEqual(cell(next, 2, 2).mine, false)
  getNeighbors(2, 2, 5, 5).concat([[2, 2]]).forEach(position => {
    assert.strictEqual(cell(next, position[0], position[1]).mine, false)
  })
  assert.strictEqual(countMines(next), 3)
})

run('空间不足时至少保证首次点击格安全', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 24, enforceLimits: false })
  const next = revealCell(game, 2, 2)
  assert.strictEqual(cell(next, 2, 2).mine, false)
  assert.strictEqual(countMines(next), 24)
})

run('翻开数字格显示相邻地雷数量', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 1 })
  const next = revealCell(game, 0, 0, {
    minePositions: [[4, 4]]
  })
  assert.strictEqual(cell(next, 3, 3).revealed, true)
  assert.strictEqual(cell(next, 3, 3).adjacentMines, 1)
})

run('翻开空白格会扩散安全区域', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 1 })
  const next = revealCell(game, 0, 0, {
    minePositions: [[4, 4]]
  })
  assert.ok(next.revealedCount > 1)
  assert.strictEqual(cell(next, 3, 3).revealed, true)
  assert.strictEqual(cell(next, 4, 4).revealed, false)
})

run('已标旗格不能被翻开', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 1 })
  const flagged = toggleFlag(game, 1, 1)
  const next = revealCell(flagged, 1, 1, {
    minePositions: [[4, 4]]
  })
  assert.strictEqual(cell(next, 1, 1).flagged, true)
  assert.strictEqual(cell(next, 1, 1).revealed, false)
})

run('标旗和取消旗标会更新剩余雷数', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 3 })
  const flagged = toggleFlag(game, 0, 0)
  assert.strictEqual(cell(flagged, 0, 0).flagged, true)
  assert.strictEqual(flagged.remainingMines, 2)
  const unflagged = toggleFlag(flagged, 0, 0)
  assert.strictEqual(cell(unflagged, 0, 0).flagged, false)
  assert.strictEqual(unflagged.remainingMines, 3)
})

run('已翻开格不能标旗', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 1 })
  const revealed = revealCell(game, 3, 3, {
    minePositions: [[4, 4]]
  })
  const next = toggleFlag(revealed, 3, 3)
  assert.strictEqual(cell(next, 3, 3).revealed, true)
  assert.strictEqual(cell(next, 3, 3).flagged, false)
})

run('踩雷后失败并记录爆炸格', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 3 })
  const started = buildBoard(game, 2, 2, {
    minePositions: [[0, 0], [0, 4], [4, 4]]
  })
  const lost = revealCell(started, 4, 4)
  assert.strictEqual(lost.status, 'lost')
  assert.deepStrictEqual(lost.explodedCell, { row: 4, col: 4 })
  assert.strictEqual(cell(lost, 4, 4).exploded, true)
})

run('翻开全部安全格后胜利并自动标记地雷', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 1 })
  const won = revealCell(game, 0, 0, {
    minePositions: [[4, 4]]
  })
  assert.strictEqual(won.status, 'won')
  assert.strictEqual(cell(won, 4, 4).flagged, true)
  assert.strictEqual(won.remainingMines, 0)
})

run('插旗数量不直接触发胜利', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 1 })
  const flagged = toggleFlag(game, 4, 4)
  assert.strictEqual(flagged.status, 'playing')
  assert.notStrictEqual(flagged.status, 'won')
})

run('结束后不能继续操作棋盘', () => {
  const game = createGame({ rows: 5, cols: 5, mines: 1 })
  const won = revealCell(game, 0, 0, {
    minePositions: [[4, 4]]
  })
  const afterReveal = revealCell(won, 4, 4)
  const afterFlag = toggleFlag(won, 4, 4)
  assert.deepStrictEqual(afterReveal, won)
  assert.deepStrictEqual(afterFlag, won)
})
