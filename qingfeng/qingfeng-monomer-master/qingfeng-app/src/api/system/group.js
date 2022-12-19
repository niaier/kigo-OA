import request from '@/utils/request'
import querystring from 'querystring'

//查询组用户信息
export function findGroupUser (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/system/group/findGroupUser?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

