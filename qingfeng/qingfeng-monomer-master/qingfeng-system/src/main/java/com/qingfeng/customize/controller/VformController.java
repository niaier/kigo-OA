package com.qingfeng.customize.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.customize.Vform;
import com.qingfeng.entity.system.UserOrganize;
import com.qingfeng.exception.MyException;
import com.qingfeng.customize.service.IVformService;
import com.qingfeng.system.service.IUserOrganizeService;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
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
* @title: VformController
* @projectName: VformController
* @description: TODO
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Slf4j
@Validated
@RestController
@RequestMapping("/customize/vform")
public class VformController extends BaseController {

    @Autowired
    private IVformService vformService;
    @Autowired
    private IUserOrganizeService userOrganizeService;
    @Autowired
    private IUserService userService;

    private static String table_schema = "qingfeng_monomer";

    /**
    * @title findListPage
    * @description 查询数据列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:30
    */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('vform:info')")
    public MyResponse findListPage(QueryRequest queryRequest, Vform vform) {
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
        vform.setAuth_user(user_id);
        vform.setAuth_organize_ids(auth_organize_ids);
        IPage<Vform> list = vformService.findListPage(vform, queryRequest);
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
    public MyResponse findList(QueryRequest queryRequest, Vform vform) {
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
        vform.setAuth_user(user_id);
        vform.setAuth_organize_ids(auth_organize_ids);
        List<Vform> vformList = vformService.findList(vform);
        return new MyResponse().data(vformList);
    }

    /**
    * @title save
    * @description 保存方法
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('vform:add')")
    public void save(@Valid @RequestBody PageData pd,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            Map maps = (Map) JSON.parse(pd.get("config").toString());

            if(maps.get("table_type").toString().equals("0")){
                //根据record判断是否为空，为空则添加，不为空则更新
                if(Verify.verifyIsNotNull(pd.get("record"))){
                    //查询数据表是否已经存在，如果存在则提示
                    Map record = (Map) JSON.parse(pd.get("record").toString());
                    if(record.get("table_name").equals(maps.get("table_name"))){
                        this.vformService.updateVform(pd);
                        json.setSuccess(true);
                        json.setMsg("数据更新成功");
                    }else{
                        QueryWrapper queryWrapper = new QueryWrapper();
                        queryWrapper.eq("table_name",maps.get("table_name"));
                        int num = vformService.list(queryWrapper).size();
                        if(num==0){
                            pd.put("table_schema",table_schema);
                            pd.put("table_name",maps.get("table_name"));
                            int n = vformService.findTableList(pd).size();
                            if(n==0){
                                //添加-创建信息
                                this.vformService.updateVform(pd);
                                json.setSuccess(true);
                                json.setMsg("数据更新成功");
                            }else{
                                json.setSuccess(false);
                                json.setMsg("数据表已创建，不可重复创建。");
                            }
                        }else{
                            json.setSuccess(false);
                            json.setMsg("数据表信息已存在，不可重复创建。");
                        }

                    }
                }else{
                    //判断数据表是否存在，存在则提示
                    QueryWrapper queryWrapper = new QueryWrapper();
                    queryWrapper.eq("table_name",maps.get("table_name"));
                    int num = vformService.list(queryWrapper).size();
                    if(num==0){
                        pd.put("table_schema",table_schema);
                        pd.put("table_name",maps.get("table_name"));
                        int n = vformService.findTableList(pd).size();
                        if(n==0){
                            //添加-创建信息
                            this.vformService.saveVform(pd);
                            json.setSuccess(true);
                            json.setMsg("新增信息成功");
                        }else{
                            json.setSuccess(false);
                            json.setMsg("数据表已创建，不可重复创建。");
                        }
                    }else{
                        json.setSuccess(false);
                        json.setMsg("数据表信息已存在，不可重复创建。");
                    }
                }
            }else if(maps.get("table_type").toString().equals("1")){
                if(Verify.verifyIsNotNull(pd.get("record"))){
                    this.vformService.updateform(pd);
                    json.setSuccess(true);
                    json.setMsg("数据更新成功");
                }else{
                    //添加-创建信息
                    this.vformService.saveform(pd);
                    json.setSuccess(true);
                    json.setMsg("新增信息成功");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = "新增信息失败";
            json.setSuccess(false);
            json.setMsg(message);
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
    @PreAuthorize("hasAnyAuthority('vform:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String[] del_ids = ids.split(StringPool.COMMA);
            this.vformService.del(del_ids);
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
    @PreAuthorize("hasAnyAuthority('vform:status')")
    public void updateStatus(@Valid @RequestBody Vform vform,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.vformService.updateById(vform);
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
     * @title getTableList
     * @description 查询表
     * @author Administrator
     * @updateTime 2022/5/2 0002 22:47
     */
    @GetMapping("/getTableList")
    public MyResponse getTableList(HttpServletRequest request) {
        PageData pd = new PageData(request);
        pd.put("table_schema",table_schema);
        List<PageData> list = vformService.findTableList(pd);
        return new MyResponse().data(list);
    }

    /**
     * @title getColumndList
     * @description 查询数据表字段
     * @author Administrator
     * @updateTime 2022/5/2 0002 23:34
     */
    @GetMapping("/getColumndList")
    public MyResponse getColumndList(HttpServletRequest request) {
        PageData pd = new PageData(request);
        pd.put("table_schema",table_schema);
        List<PageData> list = vformService.findColumndList(pd);
        return new MyResponse().data(list);
    }

}
