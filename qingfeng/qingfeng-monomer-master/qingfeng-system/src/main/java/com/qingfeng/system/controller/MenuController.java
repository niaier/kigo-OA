package com.qingfeng.system.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.system.Dictionary;
import com.qingfeng.entity.system.Menu;
import com.qingfeng.entity.system.UserOrganize;
import com.qingfeng.exception.MyException;
import com.qingfeng.system.service.IMenuService;
import com.qingfeng.system.service.IUserOrganizeService;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @ProjectName MenuController
 * @author qingfeng
 * @version 1.0.0
 * @Description 菜单信息
 * @createTime 2021/4/3 0003 21:05
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/system/menu")
public class MenuController extends BaseController {

    @Autowired
    private IMenuService menuService;
    @Autowired
    private IUserOrganizeService userOrganizeService;
    @Autowired
    private IUserService userService;

    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:05
     */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('menu:info')")
    public MyResponse findListPage(QueryRequest queryRequest, Menu menu) {
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
        menu.setAuth_user(user_id);
        menu.setAuth_organize_ids(auth_organize_ids);
        Map<String, Object> dataTable = MyUtil.getDataTable(menuService.findListPage(menu, queryRequest));
        return new MyResponse().data(dataTable);
    }

    /**
     * @title save
     * @description 保存数据
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:05
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('menu:add')")
    public void save(@Valid @RequestBody Menu menu,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            // 创建用户
            String id = GuidUtil.getUuid();
            menu.setId(id);
            String time = DateTimeUtil.getDateTimeStr();
            menu.setCreate_time(time);
            menu.setStatus("0");
            //处理数据权限
            String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
            menu.setCreate_user(authParams.split(":")[1]);
            menu.setCreate_organize(authParams.split(":")[2]);
            menu.setTitle(menu.getName());
            menu.setMenu_cascade(menu.getMenu_cascade()+id+"_");
            int level_num = Integer.parseInt(menu.getLevel_num())+1;
            menu.setLevel_num(level_num+"");
            this.menuService.save(menu);
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
     * @description 更新数据
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:06
     */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('menu:edit')")
    public void update(@Valid @RequestBody Menu menu,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            // 更新组织信息
            String time = DateTimeUtil.getDateTimeStr();
            String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
            menu.setUpdate_time(time);
            menu.setUpdate_user(authParams.split(":")[1]);
            menu.setTitle(menu.getName());
            this.menuService.updateById(menu);
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
     * @description 删除数据
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:06
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('menu:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String[] del_ids = ids.split(StringPool.COMMA);
            this.menuService.removeByIds(Arrays.asList(del_ids));
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
     * @updateTime 2021/4/3 0003 21:06
     */
    @PostMapping("/updateStatus")
    @PreAuthorize("hasAnyAuthority('menu:status')")
    public void updateStatus(@Valid @RequestBody Menu menu,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.menuService.updateById(menu);
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
     * @title: findList
     * @description: 查询菜单列表
     * @author: qingfeng
     * @date: 2021/3/9 0009 23:05
     */
    @ApiOperation("查询数据列表接口")
    @PostMapping("/findList")
    public MyResponse findList(@RequestBody PageData pd) throws IOException {
        if(Verify.verifyIsNotNull(pd.get("types"))){
            List<String> list = Arrays.asList(pd.get("types").toString().split(","));
            pd.put("typeList",list);
        }
        List<PageData> list = menuService.findList(pd);
        return new MyResponse().data(list);
    }


    @GetMapping("/findMenuList")
    public MyResponse findMenuList(HttpServletRequest request) throws IOException {
        PageData pd = new PageData(request);
        //获取当前登录信息
        String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
        String user_id = authParams.split(":")[1];
        String organize_id = authParams.split(":")[2];
        pd.put("type","1");
        pd.put("user_id",user_id);
        pd.put("organize_id",organize_id);
        List<PageData> list = menuService.findAppMenuList(pd);
        return new MyResponse().data(list);
    }

}