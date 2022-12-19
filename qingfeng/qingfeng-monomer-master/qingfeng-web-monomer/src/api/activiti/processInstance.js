import request from '@/utils/request'
import querystring from 'querystring'

//查询数据列表
export function getListPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processInstance/findListPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}
  
//中止或激活流程
export function suspensionStateUpdate(params) {
  console.log(params)
  return request({
    url: '/activiti/processInstance/suspensionState',
    method: 'post',
    data: params
  })
}

//删除数据
export function del (instanceIds) {
  console.log(instanceIds)
  return request({
    url: '/activiti/processInstance/'+instanceIds,
    method: 'delete',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


//查询执行流列表
export function findExecutionListPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processInstance/findExecutionListPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


