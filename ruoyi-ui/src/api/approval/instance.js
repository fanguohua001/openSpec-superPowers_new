import request from '@/utils/request'

export function startApproval(data) {
  return request({
    url: '/approval/instance/start',
    method: 'post',
    data: data
  })
}

export function listMyInstance(query) {
  return request({
    url: '/approval/instance/my/list',
    method: 'get',
    params: query
  })
}

export function getInstance(instanceId) {
  return request({
    url: '/approval/instance/' + instanceId,
    method: 'get'
  })
}

export function getInstanceByBusiness(query) {
  return request({
    url: '/approval/instance/business',
    method: 'get',
    params: query
  })
}
