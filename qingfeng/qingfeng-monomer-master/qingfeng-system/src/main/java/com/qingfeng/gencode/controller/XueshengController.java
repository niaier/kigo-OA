package com.qingfeng.gencode.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.gencode.Xuesheng;
import com.qingfeng.entity.system.UserOrganize;
import com.qingfeng.exception.MyException;
import com.qingfeng.gencode.service.IXueshengService;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.util.*;

/**
* @title: XueshengController
* @projectName: XueshengController
* @description: TODO
* @author: qingfeng
* @createTime 2021/4/3 0003 20:30
*/
@Slf4j
@Validated
@RestController
@RequestMapping("/gencode/xuesheng")
public class XueshengController extends BaseController {

    @Autowired
    private IXueshengService xueshengService;
    @Autowired
    private IUserOrganizeService userOrganizeService;
    @Autowired
    private IUserService userService;

    /**
    * @title findListPage
    * @description 查询数据列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:30
    */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('xuesheng:info')")
    public MyResponse findListPage(QueryRequest queryRequest, Xuesheng myxuesheng) throws Exception {
        PageData pd = new PageData();
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
        myxuesheng.setAuth_user(user_id);
        myxuesheng.setAuth_organize_ids(auth_organize_ids);
        IPage<Xuesheng> list = xueshengService.findListPage(myxuesheng, queryRequest);
        Map<String, Object> dataTable = MyUtil.getDataTable(list);
        MyResponse myResponse = new MyResponse();
        myResponse.data(dataTable);
        return myResponse;
    }

    /**
    * @title findList
    * @description 查询列表
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @GetMapping("/findList")
    public MyResponse findList(QueryRequest queryRequest, Xuesheng xuesheng) {
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
        xuesheng.setAuth_user(user_id);
        xuesheng.setAuth_organize_ids(auth_organize_ids);
        List<Xuesheng> xueshengList = xueshengService.findList(xuesheng);
        return new MyResponse().data(xueshengList);
    }

    /**
    * @title findInfo
    * @description 查询详情列表
    * @author Administrator
    * @updateTime 2021/10/6 0006 16:51
    */
    @GetMapping("/findInfo")
    public MyResponse findInfo(QueryRequest queryRequest, Xuesheng xuesheng) {
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        Xuesheng xueshengObj = xueshengService.getById(xuesheng.getId());
        return new MyResponse().data(xueshengObj);
    }

    /**
    * @title save
    * @description 保存方法
    * @author qingfeng
    * @updateTime 2021/4/3 0003 20:31
    */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('xuesheng:add')")
    public void save(@Valid @RequestBody Xuesheng xuesheng,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.xueshengService.saveXuesheng(xuesheng);
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
    @PreAuthorize("hasAnyAuthority('xuesheng:edit')")
    public void update(@Valid @RequestBody Xuesheng xuesheng,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.xueshengService.updateXuesheng(xuesheng);
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
    @PreAuthorize("hasAnyAuthority('xuesheng:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String[] del_ids = ids.split(StringPool.COMMA);
            this.xueshengService.removeByIds(Arrays.asList(del_ids));
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
    @PreAuthorize("hasAnyAuthority('xuesheng:status')")
    public void updateStatus(@Valid @RequestBody Xuesheng xuesheng,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.xueshengService.updateById(xuesheng);
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

}
