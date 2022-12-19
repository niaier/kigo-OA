package com.qingfeng.customize.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.activiti.service.ProcessDefinitionService;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.customize.service.IVformService;
import com.qingfeng.customize.service.IVmenuTfieldService;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vform;
import com.qingfeng.entity.customize.Vmenu;
import com.qingfeng.entity.system.UserOrganize;
import com.qingfeng.exception.MyException;
import com.qingfeng.customize.service.IVmenuService;
import com.qingfeng.system.service.IUserOrganizeService;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qingfeng.utils.upload.ParaUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.util.*;


/**
* @title: VmenuController
* @projectName: VmenuController
* @description: TODO
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Slf4j
@Validated
@RestController
@RequestMapping("/customize/vmenu")
public class VmenuController extends BaseController {

    @Autowired
    private IVmenuService vmenuService;
    @Autowired
    private IUserOrganizeService userOrganizeService;
    @Autowired
    private IUserService userService;
    @Autowired
    public IVmenuTfieldService vmenuTfieldService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IVformService vformService;

    /**
    * @title findListPage
    * @description 查询数据列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:30
    */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('vmenu:info')")
    public MyResponse findListPage(QueryRequest queryRequest, Vmenu vmenu) {
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        //处理数据权限
        String user_id = userParams.split(":")[1];
        UserOrganize uoParam = new UserOrganize();
        uoParam.setUser_id(user_id);
        UserOrganize userOrganize = userOrganizeService.findUserOrganizeInfo(uoParam);
        List<String> auth_organize_ids = new ArrayList<String>();
        if(Verify.verifyIsNotNull(userOrganize)){
            if(Verify.verifyIsNotNull(userOrganize.getAuthOrgIds())){
                auth_organize_ids = Arrays.asList(userOrganize.getAuthOrgIds().split(","));
            }
        }
        vmenu.setAuth_user(user_id);
        vmenu.setAuth_organize_ids(auth_organize_ids);
        IPage<Vmenu> list = vmenuService.findListPage(vmenu, queryRequest);
        for (Vmenu vm:list.getRecords()) {
            if(Verify.verifyIsNotNull(vm.getOpen_process())){
                if(vm.getOpen_process().equals("0")&&Verify.verifyIsNotNull(vm.getProcess_id())){
                    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey(vm.getProcess_id()).latestVersion().singleResult();
                    if(Verify.verifyIsNotNull(processDefinition)){
                        vm.setProcess_name(processDefinition.getName());
                    }
                }
            }
        }
        Map<String, Object> dataTable = MyUtil.getDataTable(list);
        return new MyResponse().data(dataTable);
    }

    /**
    * @title findList
    * @description 查询列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @GetMapping("/findList")
    public MyResponse findList(QueryRequest queryRequest, Vmenu vmenu) {
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        //处理数据权限
        String user_id = userParams.split(":")[1];
        UserOrganize uoParam = new UserOrganize();
        uoParam.setUser_id(user_id);
        UserOrganize userOrganize = userOrganizeService.findUserOrganizeInfo(uoParam);
        List<String> auth_organize_ids = new ArrayList<String>();
        if(Verify.verifyIsNotNull(userOrganize)){
            if(Verify.verifyIsNotNull(userOrganize.getAuthOrgIds())){
                auth_organize_ids = Arrays.asList(userOrganize.getAuthOrgIds().split(","));
            }
        }
        vmenu.setAuth_user(user_id);
        vmenu.setAuth_organize_ids(auth_organize_ids);
        List<Vmenu> vmenuList = vmenuService.findList(vmenu);
        return new MyResponse().data(vmenuList);
    }

    /**
    * @title save
    * @description 保存方法
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('vmenu:add')")
    public void save(@Valid @RequestBody Vmenu vmenu,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.vmenuService.saveVmenu(vmenu);
            json.setSuccess(true);
            json.setMsg("新增信息成功");
        } catch (Exception e) {
            String message = "新增信息失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

    /**
    * @title update
    * @description 更新方法
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('vmenu:edit')")
    public void update(@Valid @RequestBody Vmenu vmenu,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.vmenuService.updateVmenu(vmenu);
            json.setSuccess(true);
            json.setMsg("修改信息成功");
        } catch (Exception e) {
            String message = "修改信息失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

    /**
    * @title delete
    * @description 删除方法
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('vmenu:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String[] del_ids = ids.split(StringPool.COMMA);
            for (String id:del_ids) {
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("menu_id",id);
                this.vmenuTfieldService.remove(queryWrapper);
            }
            this.vmenuService.removeByIds(Arrays.asList(del_ids));
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
    * @title updateStatus
    * @description 更新状态
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @PostMapping("/updateStatus")
    @PreAuthorize("hasAnyAuthority('vmenu:status')")
    public void updateStatus(@Valid @RequestBody Vmenu vmenu,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.vmenuService.updateById(vmenu);
            json.setSuccess(true);
            json.setMsg("状态修改成功");
        } catch (Exception e) {
            String message = "状态修改失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }


    /**
     * @title getFieldList
     * @description 查询菜单管理字段列表
     * @author Administrator
     * @updateTime 2021/9/30 0030 0:19
     */
    @GetMapping("/getFieldList")
    public MyResponse getFieldList(HttpServletRequest request) {
        PageData pd = new PageData(request);
        List<PageData> list = vmenuTfieldService.findFieldList(pd);
        return new MyResponse().data(list);
    }


    /**
     * @title findMenuForm
     * @description 查询菜单-表单
     * @author Administrator
     * @updateTime 2022/6/13 0013 19:28
     */
    @GetMapping("/findMenuForm")
    public MyResponse findMenuForm(QueryRequest queryRequest, Vmenu vmenu) {
        PageData pd = new PageData();
        Vmenu vm = vmenuService.getById(vmenu.getId());
        pd.put("vmenu",vm);
        return new MyResponse().data(pd);
    }


}
