import request from '@/utils/request'
import querystring from 'querystring'

//查询数据列表
export function getListPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/bpmn/findListPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}
  
//保存或更新数据
export function saveOrUpdate (params) {
  let url = '/activiti/bpmn';
  let method = 'post';
  return request({
    url: url,
    method: method,
    data: params
  })
}

//删除数据
export function del (ids) {
  return request({
    url: '/activiti/bpmn/'+ids,
    method: 'delete',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//发布
export function publish (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/bpmn/publish?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}
