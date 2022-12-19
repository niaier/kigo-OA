import request from '@/utils/request'
import querystring from 'querystring'

//查询数据列表
export function getListPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/common/graphic/findListPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}
  
//更新状态
export function updateReadNum(data) {
  return request({
    url: '/common/graphic/updateReadNum',
    method: 'post',
    data: data
  })
}
