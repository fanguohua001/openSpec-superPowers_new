import request from '@/utils/request'
import { getToken } from '@/utils/auth'

export function listSessions(query) {
  return request({
    url: '/tool/aiChat/session/list',
    method: 'get',
    params: query
  })
}

export function listMessages(sessionId) {
  return request({
    url: '/tool/aiChat/session/' + sessionId + '/messages',
    method: 'get'
  })
}

export function deleteSession(sessionIds) {
  return request({
    url: '/tool/aiChat/session/' + sessionIds,
    method: 'delete'
  })
}

export function streamMessage(data, handlers) {
  const controller = new AbortController()
  const promise = fetch(process.env.VUE_APP_BASE_API + '/tool/aiChat/message/stream', {
    method: 'post',
    headers: {
      'Authorization': 'Bearer ' + getToken(),
      'Content-Type': 'application/json;charset=utf-8'
    },
    body: JSON.stringify(data),
    signal: controller.signal
  }).then(response => {
    if (!response.ok) {
      throw new Error('AI聊天请求失败')
    }
    return readSse(response, handlers || {})
  })

  return {
    abort: () => controller.abort(),
    promise
  }
}

async function readSse(response, handlers) {
  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const result = await reader.read()
    if (result.done) {
      break
    }
    buffer += decoder.decode(result.value, { stream: true })
    const blocks = buffer.split(/\r?\n\r?\n/)
    buffer = blocks.pop()
    blocks.forEach(block => dispatchSseBlock(block, handlers))
  }
  if (buffer) {
    dispatchSseBlock(buffer, handlers)
  }
}

function dispatchSseBlock(block, handlers) {
  const lines = block.split(/\r?\n/)
  let eventName = ''
  const dataLines = []

  lines.forEach(line => {
    if (line.indexOf('event:') === 0) {
      eventName = line.substring(6).trim()
    }
    if (line.indexOf('data:') === 0) {
      dataLines.push(line.substring(5).trim())
    }
  })

  if (!eventName) {
    return
  }

  const rawData = dataLines.join('\n')
  const data = parseSseData(rawData)
  if (handlers[eventName]) {
    handlers[eventName](data)
  }
  if (handlers.event) {
    handlers.event(eventName, data)
  }
}

function parseSseData(rawData) {
  if (!rawData) {
    return ''
  }
  try {
    return JSON.parse(rawData)
  } catch (e) {
    return rawData
  }
}
