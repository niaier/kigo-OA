package com.qingfeng.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.document.service.IDClassifyService;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.document.DClassify;
import com.qingfeng.exception.MyException;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName DClassifyController
 * @author Administrator
 * @version 1.0.0
 * @Description DClassifyController
 * @createTime 2022/6/15 0015 23:36
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/document/dclassify")
public class DClassifyController extends BaseController {

    @Autowired
    private IDClassifyService dClassifyService;
    @Autowired
    private IUserService userService;


    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author Administrator
     * @updateTime 2022/6/15 0015 23:36
     */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('dclassify:info')")
    public MyResponse findListPage(QueryRequest queryRequest, DClassify dclassify) {
        if(dclassify.getType().equals("2")){
            String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
            String user_id = userParams.split(":")[1];
            dclassify.setUser_id(user_id);
        }
        IPage<DClassify> pages = dClassifyService.findListPage(dclassify, queryRequest);
        Map<String, Object> dataTable = MyUtil.getDataTable(pages);
        return new MyResponse().data(dataTable);
    }

    /**
     * @title save
     * @description 保存方法
     * @author Administrator
     * @updateTime 2022/6/15 0015 23:36
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('dclassify:add')")
    public void save(@Valid @RequestBody DClassify dclassify,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            // 创建用户
            String id = GuidUtil.getUuid();
            dclassify.setId(id);
            String time = DateTimeUtil.getDateTimeStr();
            dclassify.setCreate_time(time);
            dclassify.setDc_cascade(dclassify.getDc_cascade()+id+"_");
            dclassify.setLevel_num((Integer.parseInt(dclassify.getLevel_num())+1)+"");
            //处理数据权限
            String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
            dclassify.setCreate_user(authParams.split(":")[1]);
            dclassify.setCreate_organize(authParams.split(":")[2]);
            this.dClassifyService.save(dclassify);
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
     * @author Administrator
     * @updateTime 2022/6/15 0015 23:36
     */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('dclassify:edit')")
    public void update(@Valid @RequestBody DClassify dclassify,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            // 更新组织信息
            String time = DateTimeUtil.getDateTimeStr();
            String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
            dclassify.setUpdate_time(time);
            dclassify.setUpdate_user(authParams.split(":")[1]);
            this.dClassifyService.updateById(dclassify);
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
     * @author Administrator
     * @updateTime 2022/6/15 0015 23:36
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('dclassify:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String[] del_ids = ids.split(StringPool.COMMA);
            this.dClassifyService.removeByIds(Arrays.asList(del_ids));
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
     * @title findList
     * @description 查询数据列表
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:03
     */
    @ApiOperation("查询数据列表接口")
    @GetMapping("/findList")
    public MyResponse findList(DClassify dclassify) throws IOException {
        if(dclassify.getType().equals("2")){
            String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
            String user_id = userParams.split(":")[1];
            dclassify.setUser_id(user_id);
        }
        List<DClassify> list = dClassifyService.findList(dclassify);
        return new MyResponse().data(list);
    }


}
