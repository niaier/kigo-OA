import request from '@/utils/request'
import querystring from 'querystring'

/**
* 查询自定义菜单数据
* @param {*} params
* @returns
*/
export function findVMenuInfo (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/customize/vdata/findVMenuInfo?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

/**
 * findVFormInfo
 * @param {*} params 
 * @returns 
 */
export function findVFormInfo (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/customize/vdata/findVFormInfo?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//查询关联表
export function findLinkFormList (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/customize/vdata/findLinkFormList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


export function findVFormData (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/customize/vdata/findVFormData?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

export function getListPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/customize/vdata/findListPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//保存或更新数据
export function saveOrUpdate (params) {
  let url = '/customize/vdata';
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


export function findVDataInfo (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/customize/vdata/findVDataInfo?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


export function delData (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/customize/vdata/delData?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//更新状态
export function updateStatus(id,status,status_type,table_name) {
  return request({
    url: '/customize/vdata/updateStatus',
    method: 'post',
    data: {
      id,
      status,
      status_type,
      table_name
    }
  })
}

//表单退回
export function rejectAnyNod (params) {
  let url = '/customize/vdata/rejectAnyNod';
  let method = 'post';
  return request({
    url: url,
    method: method,
    data: params
  })
}

//任务委派
export function delegateTask (params) {
  let url = '/customize/vdata/delegateTask';
  let method = 'post';
  return request({
    url: url,
    method: method,
    data: params
  })
}
