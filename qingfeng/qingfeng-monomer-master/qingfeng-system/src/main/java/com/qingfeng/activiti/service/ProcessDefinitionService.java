package com.qingfeng.activiti.service;


import com.qingfeng.utils.Page;

import java.io.FileNotFoundException;


/**
 * @Description:
 * @Param:
 * @return:
 * @Author: anxingtao
 * @Date: 2020-8-11 22:37
 */
public interface ProcessDefinitionService {

    /**
     * @Description: deployProcessDefinition流程部署
     * @Param: [filePath]
     * @return: void
     * @Author: anxingtao
     * @Date: 2020-8-11 22:37
     */
    void deployProcessDefinition(String filePath) throws FileNotFoundException;

    /** 
     * @Description: findProcessDefinition 查询流程定义列表
     * @Param: [pageNum, pageSize, processDefinitionKey, processDefinitionName] 
     * @return: com.wdata.base.util.Page 
     * @Author: anxingtao
     * @Date: 2020-8-18 23:44
     */ 
    Page findProcessDefinition(int pageNum, int pageSize, String processDefinitionKey, String processDefinitionName) ;
    


}
