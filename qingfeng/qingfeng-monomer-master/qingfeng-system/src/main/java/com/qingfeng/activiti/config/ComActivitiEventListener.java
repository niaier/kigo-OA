package com.qingfeng.activiti.config;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qingfeng.activiti.mapper.ActivitiEventRecordMapper;
import com.qingfeng.entity.activiti.ActivitiEventRecord;
import com.qingfeng.utils.DateTimeUtil;
import com.qingfeng.utils.DateUtil;
import com.qingfeng.utils.GuidUtil;
import com.qingfeng.utils.Verify;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Administrator
 * @version 1.0.0
 * @ProjectName qingfeng-cloud
 * @Description TODO
 * @createTime 2021年10月07日 09:49:00
 */
@Component
public class ComActivitiEventListener implements ActivitiEventListener, Serializable {

    @Autowired
    private ActivitiEventRecordMapper activitiEventRecordMapper;

    //activiti全局事件接收器
    @Override
    public void onEvent(ActivitiEvent event) {
        switch (event.getType()) {
            //流程开始
            case PROCESS_STARTED:
                proStart(event);
                break;
            //流程结束
            case PROCESS_COMPLETED:
                proOver(event);
                break;
            //任务开始
            case TASK_CREATED:
                taskCreate(event);
                break;
            //任务完成
            case TASK_COMPLETED:
                taskOver(event);
                break;
            //流程结束
            case PROCESS_CANCELLED:
                proOver(event);
            default:
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }


    /**
     * 流程开始做记录
     *
     * @param event
     */
    public void proStart(ActivitiEvent event) {
        //这个不能直接通过@Autowried注入，会报错找不到，可能本类成为bean的时候RuntimeService还没成为bean
        RuntimeService runtimeService = GetSpringBean.getBean(RuntimeService.class);
        RepositoryService repositoryService = GetSpringBean.getBean(RepositoryService.class);

        String instanceId = event.getProcessInstanceId();
        String defineId = event.getProcessDefinitionId();

        System.out.println(instanceId);
        System.out.println(defineId);

        ProcessDefinition process = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(defineId)
                .singleResult();
        System.out.println(process);
        if(Verify.verifyIsNotNull(process)){
            String proName = process.getName();

            System.out.println("=========流程启动完成【"+proName+"】=============");
            System.out.println(proName);

            //组装流程启动数据
            ActivitiEventRecord activitiEventRecord = new ActivitiEventRecord();
            activitiEventRecord.setId(GuidUtil.getUuid());
            activitiEventRecord.setType(event.getType().toString());//流程启动
            activitiEventRecord.setProcessDefinitionId(defineId);
            activitiEventRecord.setProcessDefinitionName(proName);
            activitiEventRecord.setProcessInstanceId(instanceId);
            activitiEventRecord.setRemark("流程启动完成");
            activitiEventRecord.setCreate_time(DateTimeUtil.getDateTimeStr());
            activitiEventRecordMapper.insert(activitiEventRecord);
        }
    }

    /**
     * 流程结束更新状态
     *
     * @param event
     */
    public void proOver(ActivitiEvent event) {
        RuntimeService runtimeService = GetSpringBean.getBean(RuntimeService.class);
        RepositoryService repositoryService = GetSpringBean.getBean(RepositoryService.class);
        String instanceId = event.getProcessInstanceId();
        String defineId = event.getProcessDefinitionId();

        ProcessDefinition process = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(defineId)
                .singleResult();
        String proName = process.getName();
        System.out.println("=========流程结束完成【"+proName+"】=============");
        //更新流程为完成状态
        ActivitiEventRecord activitiEventRecord = new ActivitiEventRecord();
//        activitiEventRecord.setId(GuidUtil.getUuid());
        activitiEventRecord.setType(event.getType().toString());//流程启动
        activitiEventRecord.setProcessDefinitionId(defineId);
        activitiEventRecord.setProcessDefinitionName(proName);
        activitiEventRecord.setProcessInstanceId(instanceId);
        activitiEventRecord.setRemark("流程结束完成");
        activitiEventRecord.setCreate_time(DateTimeUtil.getDateTimeStr());
        activitiEventRecordMapper.update(activitiEventRecord,new QueryWrapper<ActivitiEventRecord>().eq("processInstanceId",instanceId));
    }

    /**
     * 任务创建做记录
     *
     * @param event
     */
    public void taskCreate(ActivitiEvent event) {
        RuntimeService runtimeService = GetSpringBean.getBean(RuntimeService.class);
        RepositoryService repositoryService = GetSpringBean.getBean(RepositoryService.class);
        String instanceId = event.getProcessInstanceId();
        String defineId = event.getProcessDefinitionId();
        ActivitiEntityEvent acEntiEv = (ActivitiEntityEvent) event;
        Map eventMap = JSONObject.parseObject(JSONObject.toJSONString(acEntiEv.getEntity()), Map.class);
        String taskName = eventMap.get("name").toString();
        String taskId = eventMap.get("id").toString();
        ProcessDefinition process = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(defineId)
                .singleResult();
        String proName = process.getName();

        System.out.println("=========任务创建成功【"+taskName+"】=============");

        //封装任务数据
        ActivitiEventRecord activitiEventRecord = new ActivitiEventRecord();
        activitiEventRecord.setId(GuidUtil.getUuid());
        activitiEventRecord.setType(event.getType().toString());//流程启动
        activitiEventRecord.setProcessDefinitionId(defineId);
        activitiEventRecord.setProcessDefinitionName(proName);
        activitiEventRecord.setProcessInstanceId(instanceId);
        activitiEventRecord.setTaskId(taskId);
        activitiEventRecord.setTaskName(taskName);
        activitiEventRecord.setRemark("任务创建成功");
        activitiEventRecord.setCreate_time(DateTimeUtil.getDateTimeStr());
        activitiEventRecordMapper.insert(activitiEventRecord);
    }

    /**
     * 任务完成做更新状态
     * @param event
     */
    public void taskOver(ActivitiEvent event){
        RuntimeService runtimeService = GetSpringBean.getBean(RuntimeService.class);
        RepositoryService repositoryService = GetSpringBean.getBean(RepositoryService.class);
        String instanceId = event.getProcessInstanceId();
        String defineId = event.getProcessDefinitionId();
        ActivitiEntityEvent acEntiEv = (ActivitiEntityEvent) event;
        Map eventMap = JSONObject.parseObject(JSONObject.toJSONString(acEntiEv.getEntity()), Map.class);
        String taskName = eventMap.get("name").toString();
        String taskId = eventMap.get("id").toString();
        ProcessDefinition process = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(defineId)
                .singleResult();
        String proName = process.getName();
        System.out.println("=========任务更新成功【"+taskName+"】=============");
        //更新任务状态
        ActivitiEventRecord activitiEventRecord = new ActivitiEventRecord();
        activitiEventRecord.setId(GuidUtil.getUuid());
        activitiEventRecord.setType(event.getType().toString());//流程启动
        activitiEventRecord.setProcessDefinitionId(defineId);
        activitiEventRecord.setProcessDefinitionName(proName);
        activitiEventRecord.setProcessInstanceId(instanceId);
        activitiEventRecord.setTaskId(taskId);
        activitiEventRecord.setTaskName(taskName);
        activitiEventRecord.setRemark("任务更新成功");
        activitiEventRecord.setCreate_time(DateTimeUtil.getDateTimeStr());
        activitiEventRecordMapper.update(activitiEventRecord,new QueryWrapper<ActivitiEventRecord>().eq("processInstanceId",instanceId).eq("taskId",taskId));
    }

}
