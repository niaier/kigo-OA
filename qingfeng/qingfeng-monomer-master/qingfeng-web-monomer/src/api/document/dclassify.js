import request from '@/utils/request'
import querystring from 'querystring'

//查询数据分页列表
export function getListPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/document/dclassify/findListPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}
  
//保存或更新数据
export function saveOrUpdate (params) {
  let url = '/document/dclassify';
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

//删除数据
export function delData (ids) {
  return request({
    url: '/document/dclassify/'+ids,
    method: 'delete',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//更新状态
export function updateStatus(id,status) {
  return request({
    url: '/document/dclassify/updateStatus',
    method: 'post',
    data: {
      id,
      status
    }
  })
}

//查询列表
export async function findList (params) {
  let queryString = querystring.stringify(params);
  return await request({
    url: '/document/dclassify/findList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}




