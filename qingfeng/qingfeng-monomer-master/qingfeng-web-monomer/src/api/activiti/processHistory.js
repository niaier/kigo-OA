import request from '@/utils/request'
import querystring from 'querystring'

//历史任务查询
export function findTaskListPage(params) {
    let queryString = querystring.stringify(params);
    return request({
        url: '/activiti/processHistory/findTaskListPage?' + queryString,
        method: 'get',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        }
    })
}

//历史流程实例查询
export function findInstanceListPage(params) {
    let queryString = querystring.stringify(params);
    return request({
        url: '/activiti/processHistory/findInstanceListPage?' + queryString,
        method: 'get',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        }
    })
}

//activityIndex历史行为
export function findActivityListPage(params) {
    let queryString = querystring.stringify(params);
    return request({
        url: '/activiti/processHistory/findActivityListPage?' + queryString,
        method: 'get',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        }
    })
}

//detailIndex历史流程明细
export function findDetailListPage(params) {
    let queryString = querystring.stringify(params);
    return request({
        url: '/activiti/processHistory/findDetailListPage?' + queryString,
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

//删除历史流程实例
export function delInstance(instanceIds) {
    return request({
        url: '/activiti/processHistory/delInstance/' + instanceIds,
        method: 'delete',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        }
    })
}

//删除历史流程任务
export function delTask(taskIds) {
    return request({
        url: '/activiti/processHistory/delTask/' + taskIds,
        method: 'delete',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        }
    })
}


//获取流程的历史任务
export function getHistoryTaskList(params) {
    let queryString = querystring.stringify(params);
    return request({
        url: '/activiti/processHistory/getHistoryTaskList?' + queryString,
        method: 'get',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        }
    })
}

//获取流程的历史活动
export function getHistoryActInstanceList(params) {
    let queryString = querystring.stringify(params);
    return request({
        url: '/activiti/processHistory/getHistoryActInstanceList?' + queryString,
        method: 'get',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        }
    })
}

//获取流程历史流程变量
export function getHistoryProcessVariables(params) {
    let queryString = querystring.stringify(params);
    return request({
        url: '/activiti/processHistory/getHistoryProcessVariables?' + queryString,
        method: 'get',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        }
    })
}

//获取已归档的流程实例
export function getFinishedInstanceList(params) {
    let queryString = querystring.stringify(params);
    return request({
        url: '/activiti/processHistory/getFinishedInstanceList?' + queryString,
        method: 'get',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        }
    })
}

//获取历史流程实例（所有已发起的流程）
export function queryHistoricInstance(params) {
    let queryString = querystring.stringify(params);
    return request({
        url: '/activiti/processHistory/queryHistoricInstance?' + queryString,
        method: 'get',
        headers: {
            'Content-Type': 'application/json;charset=UTF-8',
        }
    })
}