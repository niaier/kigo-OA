import request from '@/utils/request'
import querystring from 'querystring'


//查询字典列表
export async function findDictionaryList (params) {
  let queryString = querystring.stringify(params);
  return await request({
    url: '/system/dictionary/findList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8'
    }
  })
}




