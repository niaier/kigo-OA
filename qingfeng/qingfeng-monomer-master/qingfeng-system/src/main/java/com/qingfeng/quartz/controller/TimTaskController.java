package com.qingfeng.quartz.controller;

import com.qingfeng.base.controller.BaseController;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.quartz.TimTask;
import com.qingfeng.exception.MyException;
import com.qingfeng.quartz.model.QuartzEntity;
import com.qingfeng.quartz.service.ITimTaskService;
import com.qingfeng.utils.Json;
import com.qingfeng.utils.MyUtil;
import com.qingfeng.utils.Verify;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

/**
 * @ProjectName TimTaskController
 * @author qingfeng
 * @version 1.0.0
 * @Description 定时器任务
 * @createTime 2021/4/3 0003 20:06
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/quartz/timTask")
public class TimTaskController extends BaseController {

    @Autowired @Qualifier("Scheduler")
    private Scheduler scheduler;
    @Autowired
    private ITimTaskService timTaskService;

    /**
     * @title findListPage
     * @description 查询数据列表
     * @author qingfeng
     * @updateTime 2021/4/3 0003 20:23
     */
    @GetMapping("/findListPage")
    public MyResponse findListPage(QueryRequest queryRequest, TimTask timTask) {
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        //处理数据权限
        String user_id = userParams.split(":")[1];
        Map<String, Object> dataTable = MyUtil.getDataTable(timTaskService.findListPage(timTask, queryRequest));
        return new MyResponse().data(dataTable);
    }

    /**
     * @title saveOrUpdate
     * @description 保存或更新
     * @author qingfeng
     * @updateTime 2021/4/3 0003 20:23
     */
    @PostMapping("/saveOrUpdate")
    public void saveOrUpdate(@Valid @RequestBody TimTask timTask, HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            QuartzEntity quartz = new QuartzEntity();
            quartz.setJobName(timTask.getJob_name());
            quartz.setCronExpression(timTask.getCron_expression());
            quartz.setJobClassName(timTask.getJob_class_name());
            quartz.setJobGroup(timTask.getJob_group());
            if(Verify.verifyIsNotNull(timTask.getDescription())){
                quartz.setDescription(timTask.getDescription());
            }
            if(Verify.verifyIsNotNull(timTask.getOld_job_name())){
                quartz.setOldJobName(timTask.getOld_job_name());
                quartz.setOldJobGroup(timTask.getOld_job_group());
            }
            //获取Scheduler实例、废弃、使用自动注入的scheduler、否则spring的service将无法注入
            //Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            //如果是修改  展示旧的 任务
            if(quartz.getOldJobGroup()!=null){
                JobKey key = new JobKey(quartz.getOldJobName(),quartz.getOldJobGroup());
                scheduler.deleteJob(key);
            }
            Class cls = Class.forName(quartz.getJobClassName()) ;
            cls.newInstance();
            //构建job信息
            JobDetail job = JobBuilder.newJob(cls).withIdentity(quartz.getJobName(),
                    quartz.getJobGroup())
                    .withDescription(quartz.getDescription()).build();
            // 触发时间点
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(quartz.getCronExpression());
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger"+quartz.getJobName(), quartz.getJobGroup())
                    .startNow().withSchedule(cronScheduleBuilder).build();
            //交由Scheduler安排触发
            scheduler.scheduleJob(job, trigger);
            json.setSuccess(true);
            json.setMsg("操作成功");
        } catch (Exception e) {
            String message = "操作失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

    /**
     * @title del
     * @description 删除
     * @author qingfeng
     * @updateTime 2021/4/3 0003 20:23
     */
    @GetMapping("/del")
    public void delete(@RequestParam String jobName, @RequestParam String jobGroup, HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String jobnames[] = jobName.split(",");
            String jobgroups[] = jobGroup.split(",");
            for (int i = 0; i < jobnames.length; i++) {
                TriggerKey triggerKey = TriggerKey.triggerKey(jobnames[i], jobgroups[i]);
                // 停止触发器
                scheduler.pauseTrigger(triggerKey);
                // 移除触发器
                scheduler.unscheduleJob(triggerKey);
                // 删除任务
                scheduler.deleteJob(JobKey.jobKey(jobnames[i], jobgroups[i]));
                System.out.println("removeJob:"+JobKey.jobKey(jobnames[i]));
            }
            json.setSuccess(true);
            json.setMsg("删除信息成功");
        } catch (Exception e) {
            String message = "删除信息失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }


    /**
     * @title execution
     * @description 执行
     * @author qingfeng
     * @updateTime 2021/4/3 0003 20:24
     */
    @GetMapping("/execution")
    public void execution(@RequestParam String jobName, @RequestParam String jobGroup, HttpServletResponse response) throws Exception  {
        Json json = new Json();
        try {
            JobKey key = new JobKey(jobName,jobGroup);
            scheduler.triggerJob(key);
            json.setSuccess(true);
            json.setMsg("执行成功");
        } catch (Exception e) {
            String message = "执行失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

    /**
     * @title stopOrRestore
     * @description 停止和恢复
     * @author qingfeng
     * @updateTime 2021/4/3 0003 20:24
     */
    @GetMapping("/stopOrRestore")
    public void stopOrRestore(@RequestParam String jobName, @RequestParam String jobGroup, @RequestParam String status, HttpServletResponse response) throws Exception  {
        Json json = new Json();
        try {
            JobKey key = new JobKey(jobName,jobGroup);
            if(status.equals("stop")){
                scheduler.pauseJob(key);
            }else if(status.equals("restore")){
                scheduler.resumeJob(key);
            }
            json.setSuccess(true);
            json.setMsg("执行成功");
        } catch (Exception e) {
            String message = "执行失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }


}
