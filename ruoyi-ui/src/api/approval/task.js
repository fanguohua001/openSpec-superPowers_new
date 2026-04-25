import request from '@/utils/request'

export function listPendingTask(query) {
  return request({
    url: '/approval/task/pending/list',
    method: 'get',
    params: query
  })
}

export function approveTask(data) {
  return request({
    url: '/approval/task/approve',
    method: 'post',
    data: data
  })
}

export function rejectTask(data) {
  return request({
    url: '/approval/task/reject',
    method: 'post',
    data: data
  })
}
