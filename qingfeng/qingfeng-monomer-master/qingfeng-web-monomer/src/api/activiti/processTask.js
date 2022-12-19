import request from '@/utils/request'
import querystring from 'querystring'

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
  
//claim签收任务
export function claimTask(params) {
  return request({
    url: '/activiti/processTask/claimTask',
    method: 'post',
    data: params
  })
}

//completeTask 办理任务，提交task
export function completeTask(params) {
  return request({
    url: '/activiti/processTask/completeTask',
    method: 'post',
    data: params
  })
}

//发起启动节点是消息启动类型的流程
export function messageStartEventInstance(params) {
  return request({
    url: '/activiti/processTask/messageStartEventInstance',
    method: 'post',
    data: params
  })
}

//发起启动节点是信号启动类型的流程
export function signalStartEventInstance(params) {
  return request({
    url: '/activiti/processTask/signalStartEventInstance',
    method: 'post',
    data: params
  })
}

//获取某一信号事件的所有执行 
export function getSignalEventSubscription(params) {
  console.log(params)
  return request({
    url: '/activiti/processTask/getSignalEventSubscription',
    method: 'post',
    data: params
  })
}

//获取某一消息事件的所有执行
export function getMessageEventSubscription(params) {
  return request({
    url: '/activiti/processTask/getMessageEventSubscription',
    method: 'post',
    data: params
  })
}

//消息触发
export function messageEventReceived(params) {
  return request({
    url: '/activiti/processTask/messageEventReceived',
    method: 'post',
    data: params
  })
}

//流程驳回
export function rejectAnyNode (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processTask/rejectAnyNode?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}

//委托任务
export function delegateTask (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processTask/delegateTask?'+queryString,
    method: 'get',
    headers: {
      'Content-Type': 'application/json;charset=UTF-8',
    }
  })
}


//挂起、激活流程实例
export function suspendProcessInstance(params) {
  return request({
    url: '/activiti/processTask/suspendProcessInstance',
    method: 'post',
    data: params
  })
}

//shutdown 流程终止
export function shutdownTask (params) {
  let queryString = querystring.stringify(params);
  return request({
    url: '/activiti/processTask/shutdownTask?'+queryString,
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


//查询下一节点办理人
export function completeCheck(params) {
  return request({
    url: '/activiti/processTask/completeCheck',
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

//查询可以驳回的节点
export function findFormParams (params) {
  return request({
    url: '/activiti/processTask/findFormParams',
    method: 'post',
    data: params
  })
}

