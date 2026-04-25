import request from '@/utils/request'

export function listProcess(query) {
  return request({
    url: '/approval/process/list',
    method: 'get',
    params: query
  })
}

export function addProcess(data) {
  return request({
    url: '/approval/process',
    method: 'post',
    data: data
  })
}

export function updateProcess(data) {
  return request({
    url: '/approval/process',
    method: 'put',
    data: data
  })
}

export function delProcess(processId) {
  return request({
    url: '/approval/process/' + processId,
    method: 'delete'
  })
}

export function enableProcess(processId) {
  return request({
    url: '/approval/process/' + processId + '/enable',
    method: 'put'
  })
}

export function disableProcess(processId) {
  return request({
    url: '/approval/process/' + processId + '/disable',
    method: 'put'
  })
}

export function listProcessNodes(processId) {
  return request({
    url: '/approval/process/' + processId + '/nodes',
    method: 'get'
  })
}

export function saveProcessNodes(processId, data) {
  return request({
    url: '/approval/process/' + processId + '/nodes',
    method: 'put',
    data: data
  })
}
