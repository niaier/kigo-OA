import request from '@/utils/request'
import querystring from 'querystring'

//查询数据列表
export function getListPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processDefinition/findListPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}
  
//转换流程定义为模型
export function convertToModel(params) {
  return request({
    url: '/activiti/processDefinition/convertToModel',
    method: 'post',
    data: params
  })
}

//删除数据
export function del (deploymentId) {
  return request({
    url: '/activiti/processDefinition/'+deploymentId,
    method: 'delete',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//查询流程定义列表
export function findProcessDefinition(params) {
  return request({
    url: '/activiti/processDefinition/findProcessDefinition',
    method: 'post',
    data: params
  })
}


//查询历史流程定义
export function findHistoryProcessDefinitionPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processDefinition/findHistoryProcessDefinitionPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


//启动流程引擎
export function startProcessInstanceByKey (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processDefinition/startProcessInstanceByKey?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


//判断流程模型是否存在
export function verifyIsExistModel (params) {
  let paramString = querystring.stringify(params);
  return request({
    url: '/activiti/processDefinition/verifyIsExistModel?'+paramString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


export function uploadFile (formData) {
  return request({
    url: '/activiti/processDefinition/uploadFile',
    method: 'post',
    data: formData
  })
}

