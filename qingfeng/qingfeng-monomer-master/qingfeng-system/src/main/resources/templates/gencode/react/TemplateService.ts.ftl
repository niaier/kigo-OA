import request from '@/utils/request';
import querystring from 'querystring'
import type { TableListParams, TableListItem } from './data.d';

export async function queryData(params?: TableListParams) {
  let queryString = querystring.stringify(params);
  return request('/${tablePd.mod_name}/${tablePd.bus_name}/findListPage?'+queryString, {
    data: params,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  });
}

export async function removeData(params: { ids: string[] }) {
  return request('/${tablePd.mod_name}/${tablePd.bus_name}/'+params.ids, {
    method: 'delete',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  });
}

export async function addData(params: TableListItem) {
  return request('/${tablePd.mod_name}/${tablePd.bus_name}', {
    method: 'POST',
    data: {
      ...params,
      method: 'post',
    },
  });
}

export async function updateData(params: TableListItem) {
  return request('/${tablePd.mod_name}/${tablePd.bus_name}', {
    method: 'PUT',
    data: {
      ...params,
      method: 'update',
    },
  });
}

//更新状态
export async function updateStatus(id:string,status:string) {
  return request('/${tablePd.mod_name}/${tablePd.bus_name}/updateStatus', {
    method: 'post',
    data: {
      id,
      status
    }
  });
}


//获取数据列表
export function findList (params:any) {
  let queryString = querystring.stringify(params);
  return request('/${tablePd.mod_name}/${tablePd.bus_name}/findList?'+queryString, {
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  });
}






