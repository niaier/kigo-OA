import request from '@/utils/request'
import querystring from 'querystring'


//查询菜单列表
export async function findMenuList (params) {
  let queryString = querystring.stringify(params);
  return await request({
    url: '/system/menu/findMenuList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}




