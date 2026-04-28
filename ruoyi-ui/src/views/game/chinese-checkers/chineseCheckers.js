const PLAYERS = {
  PLAYER_ONE: 'playerOne',
  PLAYER_TWO: 'playerTwo'
}

const PLAYER_LIST = [PLAYERS.PLAYER_ONE, PLAYERS.PLAYER_TWO]

const DIRECTIONS = [
  { q: 1, r: 0 },
  { q: 1, r: -1 },
  { q: 0, r: -1 },
  { q: -1, r: 0 },
  { q: -1, r: 1 },
  { q: 0, r: 1 }
]

const STATUS_PLAYING = 'playing'
const STATUS_FINISHED = 'finished'
const BOARD_RADIUS = 4
const STAR_RADIUS = 8

function createBoard() {
  const pointMap = {}
  const points = []

  function addPoint(position) {
    const key = toKey(position)
    if (!pointMap[key]) {
      const point = { q: position.q, r: position.r, key }
      pointMap[key] = point
      points.push(point)
    }
  }

  for (let q = -BOARD_RADIUS; q <= BOARD_RADIUS; q++) {
    for (let r = -BOARD_RADIUS; r <= BOARD_RADIUS; r++) {
      if (hexDistance({ q, r }) <= BOARD_RADIUS) {
        addPoint({ q, r })
      }
    }
  }

  DIRECTIONS.forEach((direction, index) => {
    const sideDirection = DIRECTIONS[(index + 2) % DIRECTIONS.length]
    for (let distance = BOARD_RADIUS + 1; distance <= STAR_RADIUS; distance++) {
      const rowLength = STAR_RADIUS + 1 - distance
      for (let offset = 0; offset < rowLength; offset++) {
        addPoint({
          q: direction.q * distance + sideDirection.q * offset,
          r: direction.r * distance + sideDirection.r * offset
        })
      }
    }
  })

  const camps = {
    [PLAYERS.PLAYER_ONE]: createCamp(DIRECTIONS[2]),
    [PLAYERS.PLAYER_TWO]: createCamp(DIRECTIONS[5])
  }

  return {
    points: points.sort(sortPositions),
    pointMap,
    camps,
    targets: {
      [PLAYERS.PLAYER_ONE]: camps[PLAYERS.PLAYER_TWO],
      [PLAYERS.PLAYER_TWO]: camps[PLAYERS.PLAYER_ONE]
    }
  }
}

function createGame(options) {
  const board = createBoard()
  const pieces = options && options.pieces
    ? Object.assign({}, options.pieces)
    : createInitialPieces(board)
  const game = {
    board,
    pieces,
    currentPlayer: PLAYERS.PLAYER_ONE,
    selectedPiece: null,
    legalMoves: [],
    chainJump: false,
    jumpHistory: [],
    status: STATUS_PLAYING,
    winner: null,
    moveCount: 0
  }
  return refreshWinner(game)
}

function selectPiece(game, position) {
  if (game.status !== STATUS_PLAYING) {
    return game
  }

  if (game.chainJump) {
    return game
  }

  const normalized = normalizePosition(position)
  if (!normalized || game.pieces[toKey(normalized)] !== game.currentPlayer) {
    return clearSelection(game)
  }

  const next = cloneGame(game)
  next.selectedPiece = normalized
  next.legalMoves = getLegalMoves(next, normalized)
  return next
}

function movePiece(game, from, to) {
  if (game.status !== STATUS_PLAYING) {
    return game
  }

  const source = normalizePosition(from)
  const target = normalizePosition(to)
  if (!source || !target) {
    return game
  }

  if (!game.selectedPiece || toKey(game.selectedPiece) !== toKey(source)) {
    return game
  }

  const targetMove = game.legalMoves.find(move => toKey(move.to) === toKey(target))
  if (!targetMove) {
    return game
  }

  const next = cloneGame(game)
  const sourceKey = toKey(source)
  const targetKey = toKey(target)
  next.pieces[targetKey] = next.pieces[sourceKey]
  delete next.pieces[sourceKey]

  if (targetMove.type === 'jump') {
    next.jumpHistory = game.chainJump
      ? game.jumpHistory.concat([targetKey])
      : [sourceKey, targetKey]
    const jumpMoves = getLegalMoves(next, target, 'jump')
    if (jumpMoves.length > 0) {
      next.selectedPiece = target
      next.legalMoves = jumpMoves
      next.chainJump = true
      return refreshWinner(next)
    }
  }

  return finishMove(next)
}

function endTurn(game) {
  if (game.status !== STATUS_PLAYING || !game.chainJump) {
    return game
  }
  return finishMove(cloneGame(game))
}

function getLegalMoves(game, position, mode) {
  const source = normalizePosition(position)
  if (!source || !game.board.pointMap[toKey(source)]) {
    return []
  }

  const moves = []
  DIRECTIONS.forEach(direction => {
    const neighbor = add(source, direction)
    const landing = add(source, multiply(direction, 2))
    const neighborKey = toKey(neighbor)
    const landingKey = toKey(landing)

    if (mode !== 'jump' && game.board.pointMap[neighborKey] && !game.pieces[neighborKey]) {
      moves.push({
        type: 'step',
        to: withKey(neighbor)
      })
    }

    if (game.board.pointMap[landingKey] && game.pieces[neighborKey] && !game.pieces[landingKey]) {
      if (mode === 'jump' && game.jumpHistory.indexOf(landingKey) !== -1) {
        return
      }
      moves.push({
        type: 'jump',
        to: withKey(landing),
        over: withKey(neighbor)
      })
    }
  })

  return mode === 'jump' ? moves.filter(move => move.type === 'jump') : moves
}

function isWinner(game, player) {
  const targetKeys = new Set(game.board.targets[player].map(toKey))
  const playerKeys = Object.keys(game.pieces).filter(key => game.pieces[key] === player)
  return playerKeys.length === 10 && playerKeys.every(key => targetKeys.has(key))
}

function finishMove(game) {
  game.moveCount += 1
  game.currentPlayer = nextPlayer(game.currentPlayer)
  game.selectedPiece = null
  game.legalMoves = []
  game.chainJump = false
  game.jumpHistory = []
  return refreshWinner(game)
}

function refreshWinner(game) {
  const winner = PLAYER_LIST.find(player => isWinner(game, player))
  if (winner) {
    game.status = STATUS_FINISHED
    game.winner = winner
    game.selectedPiece = null
    game.legalMoves = []
    game.chainJump = false
    game.jumpHistory = []
  }
  return game
}

function clearSelection(game) {
  const next = cloneGame(game)
  next.selectedPiece = null
  next.legalMoves = []
  return next
}

function createInitialPieces(board) {
  const pieces = {}
  board.camps[PLAYERS.PLAYER_ONE].forEach(position => {
    pieces[toKey(position)] = PLAYERS.PLAYER_ONE
  })
  board.camps[PLAYERS.PLAYER_TWO].forEach(position => {
    pieces[toKey(position)] = PLAYERS.PLAYER_TWO
  })
  return pieces
}

function createCamp(direction) {
  const directionIndex = DIRECTIONS.findIndex(item => item.q === direction.q && item.r === direction.r)
  const sideDirection = DIRECTIONS[(directionIndex + 2) % DIRECTIONS.length]
  const camp = []

  for (let distance = BOARD_RADIUS + 1; distance <= STAR_RADIUS; distance++) {
    const rowLength = STAR_RADIUS + 1 - distance
    for (let offset = 0; offset < rowLength; offset++) {
      camp.push({
        q: direction.q * distance + sideDirection.q * offset,
        r: direction.r * distance + sideDirection.r * offset
      })
    }
  }

  return camp.sort(sortPositions)
}

function cloneGame(game) {
  return {
    board: game.board,
    pieces: Object.assign({}, game.pieces),
    currentPlayer: game.currentPlayer,
    selectedPiece: game.selectedPiece ? normalizePosition(game.selectedPiece) : null,
    legalMoves: game.legalMoves.map(move => Object.assign({}, move, {
      to: normalizePosition(move.to),
      over: move.over ? normalizePosition(move.over) : undefined
    })),
    chainJump: game.chainJump,
    jumpHistory: game.jumpHistory ? game.jumpHistory.slice() : [],
    status: game.status,
    winner: game.winner,
    moveCount: game.moveCount
  }
}

function nextPlayer(player) {
  return player === PLAYERS.PLAYER_ONE ? PLAYERS.PLAYER_TWO : PLAYERS.PLAYER_ONE
}

function add(position, direction) {
  return {
    q: position.q + direction.q,
    r: position.r + direction.r
  }
}

function multiply(direction, factor) {
  return {
    q: direction.q * factor,
    r: direction.r * factor
  }
}

function normalizePosition(position) {
  if (!position) {
    return null
  }
  return {
    q: Number(position.q),
    r: Number(position.r)
  }
}

function withKey(position) {
  return Object.assign({}, position, {
    key: toKey(position)
  })
}

function toKey(position) {
  return position.q + ',' + position.r
}

function hexDistance(position) {
  return Math.max(Math.abs(position.q), Math.abs(position.r), Math.abs(-position.q - position.r))
}

function sortPositions(a, b) {
  if (a.r !== b.r) {
    return a.r - b.r
  }
  return a.q - b.q
}

module.exports = {
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
}
