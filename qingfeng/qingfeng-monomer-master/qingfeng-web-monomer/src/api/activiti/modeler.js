import request from '@/utils/request'
import querystring from 'querystring'

//查询数据列表
export function getListPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/modeler/findListPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}
  
//保存流程模型
export function save (params) {
  let paramString = querystring.stringify(params);
  return request({
    url: '/activiti/modeler/save?'+paramString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//发布流程模型
export function publish (params) {
  let paramString = querystring.stringify(params);
  console.log(paramString)
  return request({
    url: '/activiti/modeler/publish?'+paramString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


//撤销流程发布
export function revokePublish (params) {
  let paramString = querystring.stringify(params);
  return request({
    url: '/activiti/modeler/revokePublish?'+paramString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


//删除数据
export function del (ids) {
  return request({
    url: '/activiti/modeler/'+ids,
    method: 'delete',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//更新状态
export function updateStatus(id,status) {
  return request({
    url: '/activiti/modeler/updateStatus',
    method: 'post',
    data: {
      id,
      status
    }
  })
}

//更新权限
export function updateAuth (params) {
  return request({
    url: '/activiti/modeler/updateAuth',
    method: 'post',
    data: params
  })
}

//获取角色菜单列表
export function findmodelerMenuList (params) {
  return request({
    url: '/activiti/modeler/findmodelerMenuList',
    method: 'post',
    data: params
  })
}

//查询  
export function getServiceList (parameter) {
  return request({
    url: "",
    method: 'get',
    params: parameter
  })
}

//查询流程审批节点
export function findNodes (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/modeler/findNodes?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


//节点办理设置
export function findAssignment (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/modeler/findAssignment?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//保存流程节点办理人
export function saveAssignment (params) {
  console.log(params)
  return request({
    url: '/activiti/modeler/saveAssignment',
    method: 'post',
    data: params
  })
}




//bpmnjs流程设计器
export function deployByString (params) {
  return request({
    url: '/activiti/modeler/deployByString',
    method: 'post',
    data: params
  })
}

//查询流程办理人-组的名称
export function findUserOrOrganizeNames (params) {
  return request({
    url: '/activiti/modeler/findUserOrOrganizeNames',
    method: 'post',
    data: params
  })
}
  

//查询用户组信息
export function findGroupList (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/modeler/findGroupList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


export function findFormList (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/modeler/findFormList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

export function findFieldList (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/modeler/findFieldList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//查询流程字段信息
export function findProcessField (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/modeler/findProcessField?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

