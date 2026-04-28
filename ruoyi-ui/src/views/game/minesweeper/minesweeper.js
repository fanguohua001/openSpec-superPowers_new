const DIFFICULTIES = {
  beginner: { rows: 9, cols: 9, mines: 10 },
  intermediate: { rows: 16, cols: 16, mines: 40 },
  expert: { rows: 16, cols: 30, mines: 99 }
}

const STATUS_READY = 'ready'
const STATUS_PLAYING = 'playing'
const STATUS_WON = 'won'
const STATUS_LOST = 'lost'

function createGame(options) {
  const config = normalizeOptions(options || {})
  const game = {
    difficulty: config.difficulty,
    rows: config.rows,
    cols: config.cols,
    mines: config.mines,
    status: STATUS_READY,
    started: false,
    boardReady: false,
    flagCount: 0,
    remainingMines: config.mines,
    revealedCount: 0,
    explodedCell: null,
    cells: createEmptyCells(config.rows, config.cols)
  }
  return refreshCounts(game)
}

function normalizeOptions(options) {
  const difficulty = options.difficulty || options.level
  if (difficulty && difficulty !== 'custom' && DIFFICULTIES[difficulty]) {
    return Object.assign({ difficulty }, DIFFICULTIES[difficulty])
  }

  if (options.enforceLimits === false) {
    const rows = Math.max(1, toInteger(options.rows, 9))
    const cols = Math.max(1, toInteger(options.cols, 9))
    const maxMines = Math.max(1, rows * cols - 1)
    return {
      difficulty: 'custom',
      rows,
      cols,
      mines: clamp(toInteger(options.mines, Math.min(10, maxMines)), 1, maxMines)
    }
  }

  const rows = clamp(toInteger(options.rows, 9), 5, 24)
  const cols = clamp(toInteger(options.cols, 9), 5, 40)
  const maxMines = Math.max(1, rows * cols - 9)
  const mines = clamp(toInteger(options.mines, Math.min(10, maxMines)), 1, maxMines)

  return {
    difficulty: difficulty || 'custom',
    rows,
    cols,
    mines
  }
}

function revealCell(game, row, col, buildOptions) {
  if (!isInside(game, row, col) || isEnded(game)) {
    return game
  }

  let next = cloneGame(game)
  let target = next.cells[row][col]
  if (target.flagged || target.revealed) {
    return next
  }

  if (!next.boardReady) {
    next = buildBoard(next, row, col, buildOptions || {})
    target = next.cells[row][col]
  }

  next.started = true
  if (next.status === STATUS_READY) {
    next.status = STATUS_PLAYING
  }

  if (target.mine) {
    target.revealed = true
    target.exploded = true
    next.status = STATUS_LOST
    next.explodedCell = { row, col }
    revealEndState(next)
    return refreshCounts(next)
  }

  revealSafeArea(next, row, col)
  refreshCounts(next)

  if (next.revealedCount === next.rows * next.cols - next.mines) {
    markWon(next)
  }

  return refreshCounts(next)
}

function toggleFlag(game, row, col) {
  if (!isInside(game, row, col) || isEnded(game)) {
    return game
  }

  const next = cloneGame(game)
  const target = next.cells[row][col]
  if (target.revealed) {
    return next
  }

  target.flagged = !target.flagged
  next.started = true
  if (next.status === STATUS_READY) {
    next.status = STATUS_PLAYING
  }

  return refreshCounts(next)
}

function buildBoard(game, firstRow, firstCol, options) {
  const next = cloneGame(game)
  const buildOptions = options || {}

  next.cells.forEach(row => {
    row.forEach(cell => {
      cell.mine = false
      cell.adjacentMines = 0
      cell.exploded = false
      cell.wrongFlag = false
    })
  })

  const fullSafeKeys = new Set(
    getNeighbors(firstRow, firstCol, next.rows, next.cols)
      .concat([[firstRow, firstCol]])
      .map(position => toKey(position[0], position[1]))
  )
  const clickedKey = toKey(firstRow, firstCol)
  const allPositions = listPositions(next.rows, next.cols)
  let candidates = allPositions.filter(position => !fullSafeKeys.has(toKey(position.row, position.col)))

  if (candidates.length < next.mines) {
    candidates = allPositions.filter(position => toKey(position.row, position.col) !== clickedKey)
  }

  const minePositions = selectMinePositions(candidates, next.mines, buildOptions)
  minePositions.forEach(position => {
    next.cells[position.row][position.col].mine = true
  })

  next.cells.forEach(row => {
    row.forEach(cell => {
      if (!cell.mine) {
        cell.adjacentMines = getNeighbors(cell.row, cell.col, next.rows, next.cols)
          .filter(position => next.cells[position[0]][position[1]].mine)
          .length
      }
    })
  })

  next.boardReady = true
  return refreshCounts(next)
}

function getNeighbors(row, col, rows, cols) {
  const neighbors = []
  for (let rowOffset = -1; rowOffset <= 1; rowOffset++) {
    for (let colOffset = -1; colOffset <= 1; colOffset++) {
      if (rowOffset === 0 && colOffset === 0) {
        continue
      }
      const nextRow = row + rowOffset
      const nextCol = col + colOffset
      if (nextRow >= 0 && nextRow < rows && nextCol >= 0 && nextCol < cols) {
        neighbors.push([nextRow, nextCol])
      }
    }
  }
  return neighbors
}

function revealSafeArea(game, row, col) {
  const queue = [[row, col]]
  const visited = new Set()

  while (queue.length) {
    const position = queue.shift()
    const currentRow = position[0]
    const currentCol = position[1]
    const currentKey = toKey(currentRow, currentCol)
    const current = game.cells[currentRow][currentCol]

    if (visited.has(currentKey) || current.revealed || current.flagged || current.mine) {
      continue
    }

    visited.add(currentKey)
    current.revealed = true

    if (current.adjacentMines === 0) {
      getNeighbors(currentRow, currentCol, game.rows, game.cols).forEach(nextPosition => {
        const nextCell = game.cells[nextPosition[0]][nextPosition[1]]
        if (!nextCell.revealed && !nextCell.flagged && !nextCell.mine) {
          queue.push(nextPosition)
        }
      })
    }
  }
}

function markWon(game) {
  game.status = STATUS_WON
  game.started = true
  game.cells.forEach(row => {
    row.forEach(cell => {
      if (cell.mine && !cell.revealed) {
        cell.flagged = true
      }
      cell.wrongFlag = false
      cell.exploded = false
    })
  })
}

function revealEndState(game) {
  game.cells.forEach(row => {
    row.forEach(cell => {
      if (cell.mine) {
        cell.revealed = true
      }
      if (cell.flagged && !cell.mine) {
        cell.wrongFlag = true
      }
    })
  })
}

function selectMinePositions(candidates, mineCount, options) {
  const candidateKeys = new Set(candidates.map(position => toKey(position.row, position.col)))
  const selected = []
  const selectedKeys = new Set()

  if (Array.isArray(options.minePositions)) {
    options.minePositions.forEach(position => {
      const row = Array.isArray(position) ? position[0] : position.row
      const col = Array.isArray(position) ? position[1] : position.col
      const positionKey = toKey(row, col)
      if (candidateKeys.has(positionKey) && !selectedKeys.has(positionKey) && selected.length < mineCount) {
        selected.push({ row, col })
        selectedKeys.add(positionKey)
      }
    })
  }

  const remaining = candidates.filter(position => !selectedKeys.has(toKey(position.row, position.col)))
  shuffle(remaining, options.random || Math.random)

  while (selected.length < mineCount && remaining.length) {
    const position = remaining.shift()
    selected.push({ row: position.row, col: position.col })
  }

  return selected
}

function shuffle(items, random) {
  for (let index = items.length - 1; index > 0; index--) {
    const nextIndex = Math.floor(random() * (index + 1))
    const current = items[index]
    items[index] = items[nextIndex]
    items[nextIndex] = current
  }
}

function createEmptyCells(rows, cols) {
  const cells = []
  for (let row = 0; row < rows; row++) {
    const line = []
    for (let col = 0; col < cols; col++) {
      line.push({
        row,
        col,
        mine: false,
        adjacentMines: 0,
        revealed: false,
        flagged: false,
        exploded: false,
        wrongFlag: false
      })
    }
    cells.push(line)
  }
  return cells
}

function refreshCounts(game) {
  let flagCount = 0
  let revealedCount = 0
  game.cells.forEach(row => {
    row.forEach(cell => {
      if (cell.flagged) {
        flagCount++
      }
      if (cell.revealed && !cell.mine) {
        revealedCount++
      }
    })
  })
  game.flagCount = flagCount
  game.revealedCount = revealedCount
  game.remainingMines = game.mines - flagCount
  return game
}

function cloneGame(game) {
  return Object.assign({}, game, {
    explodedCell: game.explodedCell ? Object.assign({}, game.explodedCell) : null,
    cells: game.cells.map(row => row.map(cell => Object.assign({}, cell)))
  })
}

function isInside(game, row, col) {
  return row >= 0 && row < game.rows && col >= 0 && col < game.cols
}

function isEnded(game) {
  return game.status === STATUS_WON || game.status === STATUS_LOST
}

function listPositions(rows, cols) {
  const positions = []
  for (let row = 0; row < rows; row++) {
    for (let col = 0; col < cols; col++) {
      positions.push({ row, col })
    }
  }
  return positions
}

function toInteger(value, fallback) {
  const numberValue = Number(value)
  if (!Number.isFinite(numberValue)) {
    return fallback
  }
  return Math.floor(numberValue)
}

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max)
}

function toKey(row, col) {
  return row + ',' + col
}

module.exports = {
  DIFFICULTIES,
  createGame,
  normalizeOptions,
  revealCell,
  toggleFlag,
  buildBoard,
  getNeighbors
}
