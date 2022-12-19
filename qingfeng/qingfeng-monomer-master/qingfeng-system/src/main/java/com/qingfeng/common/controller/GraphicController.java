package com.qingfeng.common.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.common.service.IGraphicService;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.common.Graphic;
import com.qingfeng.exception.MyException;
import com.qingfeng.system.service.*;
import com.qingfeng.utils.*;
import com.qingfeng.utils.upload.ParaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.*;

/**
 * @ProjectName GraphicController
 * @author Administrator
 * @version 1.0.0
 * @Description graphic
 * @createTime 2022/6/3 0003 22:53
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/common/graphic")
public class GraphicController extends BaseController {

    @Autowired
    private IGraphicService graphicService;
    @Autowired
    private IUserService userService;


    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:08
     */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('graphic:info')")
    public MyResponse findListPage(QueryRequest queryRequest, Graphic graphic) {
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        IPage<Graphic> pages = graphicService.findListPage(graphic, queryRequest);
        for (Graphic g:pages.getRecords()) {
            g.setShow_tpdz(ParaUtil.cloudfile+g.getTpdz());
        }
        Map<String, Object> dataTable = MyUtil.getDataTable(pages);
        return new MyResponse().data(dataTable);
    }

    /**
     * @title save
     * @description 保存方法
     * @author qingfeng
     * @updateTime 2021/4/3 0003 21:08
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('graphic:add')")
    public void save(@Valid @RequestBody Graphic graphic,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            // 创建用户
            String id = GuidUtil.getUuid();
            graphic.setId(id);
            String time = DateTimeUtil.getDateTimeStr();
            graphic.setCreate_time(time);
            graphic.setStatus("0");
            graphic.setRead_num("0");
            //处理数据权限
            String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
            graphic.setCreate_user(authParams.split(":")[1]);
            graphic.setCreate_organize(authParams.split(":")[2]);
            this.graphicService.save(graphic);
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
     * @updateTime 2021/4/3 0003 21:09
     */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('graphic:edit')")
    public void update(@Valid @RequestBody Graphic graphic,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            // 更新组织信息
            String time = DateTimeUtil.getDateTimeStr();
            String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
            graphic.setUpdate_time(time);
            graphic.setUpdate_user(authParams.split(":")[1]);
            this.graphicService.updateById(graphic);
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
     * @updateTime 2021/4/3 0003 21:09
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('graphic:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String[] del_ids = ids.split(StringPool.COMMA);
            this.graphicService.removeByIds(Arrays.asList(del_ids));
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
     * @updateTime 2021/4/3 0003 21:09
     */
    @PostMapping("/updateStatus")
    @PreAuthorize("hasAnyAuthority('graphic:status')")
    public void updateStatus(@Valid @RequestBody Graphic graphic,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.graphicService.updateById(graphic);
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
     * @title updateReadNum
     * @description 更新阅读量
     * @author Administrator
     * @updateTime 2022/6/10 0010 0:37
     */
    @PostMapping("/updateReadNum")
    @PreAuthorize("hasAnyAuthority('graphic:edit')")
    public void updateReadNum(@Valid @RequestBody Graphic graphic,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            Graphic g = graphicService.getById(graphic.getId());
            g.setRead_num((Integer.parseInt(g.getRead_num())+1)+"");
            this.graphicService.updateById(g);
            json.setSuccess(true);
        } catch (Exception e) {
            json.setSuccess(false);
            throw new MyException(e.toString());
        }
        this.writeJson(response,json);
    }

}
