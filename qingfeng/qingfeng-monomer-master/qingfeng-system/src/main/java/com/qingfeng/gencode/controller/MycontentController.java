package com.qingfeng.gencode.controller;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.common.service.IUploadService;
import com.qingfeng.entity.common.UploadFile;
import com.qingfeng.entity.gencode.Mycontent;
import com.qingfeng.entity.system.UserOrganize;
import com.qingfeng.exception.MyException;
import com.qingfeng.gencode.service.IMycontentService;
import com.qingfeng.system.service.IUserOrganizeService;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import lombok.extern.slf4j.Slf4j;
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
 * @ProjectName MycontentController
 * @author qingfeng
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/4/3 0003 20:30
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/gencode/mycontent")
public class MycontentController extends BaseController {

    @Autowired
    private IMycontentService mycontentService;
    @Autowired
    private IUserOrganizeService userOrganizeService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IUploadService uploadService;

    /**
     * @title findListPage
     * @description 查询数据列表
     * @author qingfeng
     * @updateTime 2021/4/3 0003 20:30
     */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('mycontent:info')")
    public MyResponse findListPage(QueryRequest queryRequest, Mycontent mycontent) {
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
        mycontent.setAuth_user(user_id);
        mycontent.setAuth_organize_ids(auth_organize_ids);
        IPage<Mycontent> list = mycontentService.findListPage(mycontent, queryRequest);
        for (Mycontent mycontentObj:list.getRecords()) {
            //查询简介附件信息
            if(Verify.verifyIsNotNull(mycontentObj.getIntro())){
                Collection<UploadFile> introFileList = uploadService.listByIds(Arrays.asList(mycontentObj.getIntro().split(",")));
                mycontentObj.setIntroFileList(introFileList);
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
    public MyResponse findList(QueryRequest queryRequest, Mycontent mycontent) {
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
        mycontent.setAuth_user(user_id);
        mycontent.setAuth_organize_ids(auth_organize_ids);
        List<Mycontent> mycontentList = mycontentService.findList(mycontent);
        for (Mycontent mycontentObj:mycontentList) {
            //查询简介附件信息
            Collection<UploadFile> introFileList = uploadService.listByIds(Arrays.asList(mycontentObj.getIntro().split(",")));
            mycontentObj.setIntroFileList(introFileList);
        }
        return new MyResponse().data(mycontentList);
    }

    /**
     * @title save
     * @description 保存方法
     * @author qingfeng
     * @updateTime 2021/4/3 0003 20:31
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('mycontent:add')")
    public void save(@Valid @RequestBody Mycontent mycontent, HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.mycontentService.saveMycontent(mycontent);
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
    @PreAuthorize("hasAnyAuthority('mycontent:edit')")
    public void update(@Valid @RequestBody Mycontent mycontent, HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.mycontentService.updateMycontent(mycontent);
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
    @PreAuthorize("hasAnyAuthority('mycontent:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids, HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String[] del_ids = ids.split(StringPool.COMMA);
            //删除主表附件信息
            for (String id:del_ids) {
                QueryWrapper queryWrapper = new QueryWrapper();
                queryWrapper.eq("obj_id",id);
                List<UploadFile> fileList = uploadService.list(queryWrapper);
                for (UploadFile fPd:fileList) {
                    //查询信息
                    File pathFile = new File(ParaUtil.localName+fPd.getFile_path());
                    pathFile.delete();
                    pathFile.deleteOnExit();
                    uploadService.removeById(fPd.getId());
                }
            }
            this.mycontentService.removeByIds(Arrays.asList(del_ids));
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
    @PreAuthorize("hasAnyAuthority('mycontent:status')")
    public void updateStatus(@Valid @RequestBody Mycontent mycontent, HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            this.mycontentService.updateById(mycontent);
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
