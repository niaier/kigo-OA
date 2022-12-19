import request from '@/utils/request'
import querystring from 'querystring'


//历史任务查询
export function findHistoryTaskListPage(params) {
  let queryString = querystring.stringify(params);
  return request({
      url: '/activiti/processHistory/findTaskListPage?' + queryString,
      method: 'get',
      headers: {
          'Content-Type': 'application/json;charset=UTF-8',
      }
  })
}

//我发起的流程
export function findMyInstanceListPage(params) {
  let queryString = querystring.stringify(params);
  return request({
      url: '/activiti/processHistory/findMyInstanceListPage?' + queryString,
      method: 'get',
      headers: {
          'Content-Type': 'application/json;charset=UTF-8',
      }
  })
}



//查询流程跟踪单
export function findActivityList (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processTask/findActivityList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//查询当前流程任务
export function findActivityTaskList (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processTask/findActivityTaskList?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


//获取当前人员待办列表【获取当前人员待办列表,如果要查询所有，则type传allTask,个人传userTask】
export function findTaskListPage (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processTask/findTaskListPage?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


//查询可以驳回的节点
export function findFormParams (params) {
  console.log('------')
  console.log(params)
  return request({
    url: '/activiti/processTask/findFormParams',
    method: 'post',
    data: params
  })
}

//claim签收任务
export function claimTask(params) {
  return request({
    url: '/activiti/processTask/claimTask',
    method: 'post',
    data: params
  })
}

//查询可以驳回的节点
export function getRunNodes (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processTask/getRunNodes?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//查询下一节点办理人
export function findNextAssignment(params) {
  return request({
    url: '/activiti/processTask/findNextAssignment',
    method: 'post',
    data: params
  })
}

