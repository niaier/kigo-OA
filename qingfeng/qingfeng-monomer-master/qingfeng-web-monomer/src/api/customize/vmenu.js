import request from '@/utils/request'
import querystring from 'querystring'

/**
* 查询数据分页列表
* @param {*} params
* @returns
*/
export function getListPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/customize/vmenu/findListPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

/**
* 保存或更新数据
* @param {*} params
* @returns
*/
export function saveOrUpdate (params) {
  let url = '/customize/vmenu';
  let method = 'post';
  if(params.id!=''&&params.id!=undefined){
    method = 'put';
  }
  return request({
    url: url,
    method: method,
    data: params
  })
}

/**
* 删除数据
* @param {*} ids
* @returns
*/
export function delData (ids) {
  return request({
    url: '/customize/vmenu/'+ids,
    method: 'delete',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

/**
* 更新状态
* @param {*} id
* @param {*} status
* @returns
*/
export function updateStatus(id,status) {
  return request({
    url: '/customize/vmenu/updateStatus',
    method: 'post',
    data: {
      id,
      status
    }
  })
}

/**
* 查询数据列表
* @param {*} params
* @returns
*/
export function getList (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/customize/vmenu/findList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//查询字段列表
export function getFieldList (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/customize/vmenu/getFieldList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//查询流程定义列表
export function getDefinitionList (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processDefinition/findDefinitionList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}