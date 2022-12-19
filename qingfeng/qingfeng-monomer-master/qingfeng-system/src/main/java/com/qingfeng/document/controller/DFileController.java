package com.qingfeng.document.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.qingfeng.base.controller.BaseController;
import com.qingfeng.common.dto.FileChunkDTO;
import com.qingfeng.common.dto.FileChunkResultDTO;
import com.qingfeng.common.response.RestApiResponse;
import com.qingfeng.common.service.IUploadBlockService;
import com.qingfeng.document.service.IDFileService;
import com.qingfeng.document.service.IDShareRecordService;
import com.qingfeng.entity.MyResponse;
import com.qingfeng.entity.QueryRequest;
import com.qingfeng.entity.document.DClassify;
import com.qingfeng.entity.document.DFile;
import com.qingfeng.entity.document.DShareRecord;
import com.qingfeng.entity.system.Dictionary;
import com.qingfeng.exception.MyException;
import com.qingfeng.system.service.IUserService;
import com.qingfeng.utils.*;
import com.qingfeng.utils.upload.ParaUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName DFileController
 * @author Administrator
 * @version 1.0.0
 * @Description 文件上传
 * @createTime 2022/6/15 0015 23:40
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/document/dfile")
public class DFileController extends BaseController {

    @Autowired
    private IDFileService dFileService;
    @Autowired
    private IDShareRecordService shareRecordService;
    @Autowired
    private IUploadBlockService uploadBlockService;

    private String path = "/document/"+ DateTimeUtil.getDate()+"/";


    /**
     * @title findListPage
     * @description 查询数据分页列表
     * @author Administrator
     * @updateTime 2022/6/15 0015 23:40
     */
    @GetMapping("/findListPage")
    @PreAuthorize("hasAnyAuthority('dfile:info')")
    public MyResponse findListPage(QueryRequest queryRequest, DFile dfile) {
        if(dfile.getType().equals("2")){
            String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
            String user_id = userParams.split(":")[1];
            dfile.setUser_id(user_id);
        }
        if(dfile.getClassify_id().equals("1")){
            dfile.setClassify_id("");
        }
        IPage<DFile> pages = dFileService.findListPage(dfile, queryRequest);
        Map<String, Object> dataTable = MyUtil.getDataTable(pages);
        return new MyResponse().data(dataTable);
    }

    /**
     * @title save
     * @description 保存方法
     * @author Administrator
     * @updateTime 2022/6/15 0015 23:40
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('dfile:add')")
    public void save(@Valid @RequestBody DFile dfile,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            // 创建用户
            String id = GuidUtil.getUuid();
            dfile.setId(id);
            String time = DateTimeUtil.getDateTimeStr();
            dfile.setCreate_time(time);
            //处理数据权限
            String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
            dfile.setCreate_user(authParams.split(":")[1]);
            dfile.setCreate_organize(authParams.split(":")[2]);
            this.dFileService.save(dfile);
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
     * @updateTime 2022/6/15 0015 23:41
     */
    @PutMapping
    @PreAuthorize("hasAnyAuthority('dfile:edit')")
    public void update(@Valid @RequestBody DFile dfile,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            // 更新组织信息
            String time = DateTimeUtil.getDateTimeStr();
            String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
            dfile.setUpdate_time(time);
            dfile.setUpdate_user(authParams.split(":")[1]);
            this.dFileService.updateById(dfile);
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
     * @updateTime 2022/6/15 0015 23:41
     */
    @DeleteMapping("/{ids}")
    @PreAuthorize("hasAnyAuthority('dfile:del')")
    public void delete(@NotBlank(message = "{required}") @PathVariable String ids,HttpServletResponse response) throws Exception {
        Json json = new Json();
        try {
            String[] del_ids = ids.split(StringPool.COMMA);
            this.dFileService.removeByIds(Arrays.asList(del_ids));
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
    public MyResponse findList(DFile dFile) throws IOException {
        if(dFile.getType().equals("2")){
            String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
            String user_id = userParams.split(":")[1];
            dFile.setUser_id(user_id);
        }
        List<DFile> list = dFileService.findList(dFile);
        return new MyResponse().data(list);
    }

    @PostMapping("/updateShare")
    @PreAuthorize("hasAnyAuthority('dfile:edit')")
    public void updateShare(@Valid @RequestBody DFile dFile, HttpServletResponse response) throws Exception {
        Json json = new Json();
        String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
        String user_id = userParams.split(":")[1];
        try {
            String time = DateTimeUtil.getDateTimeStr();
            dFile.setShare_time(time);
            dFile.setUpdate_time(time);
            this.dFileService.updateById(dFile);
            if(dFile.getIs_share().equals("0")){
                DShareRecord shareRecord = new DShareRecord();
                shareRecord.setId(GuidUtil.getUuid());
                shareRecord.setType("1");//取消分享
                shareRecord.setDfile_id(dFile.getId());
                shareRecord.setCreate_user(user_id);
                shareRecord.setCreate_time(time);
                shareRecordService.save(shareRecord);
            }else{
                DShareRecord shareRecord = new DShareRecord();
                shareRecord.setId(GuidUtil.getUuid());
                shareRecord.setType("0");//开始分享
                shareRecord.setDfile_id(dFile.getId());
                shareRecord.setCreate_user(user_id);
                shareRecord.setCreate_time(time);
                shareRecordService.save(shareRecord);
            }
            json.setSuccess(true);
            json.setMsg("修改成功");
        } catch (Exception e) {
            String message = "修改失败";
            json.setSuccess(false);
            json.setMsg(message);
            log.error(message, e);
            throw new MyException(message);
        }
        this.writeJson(response,json);
    }

    //====================处理附件上传=====================

    /**
     * 检查分片是否存在
     *
     * @return
     */
    @GetMapping("chunk")
    public RestApiResponse<Object> checkChunkExist(FileChunkDTO chunkDTO) {
        FileChunkResultDTO fileChunkCheckDTO;
        try {
            chunkDTO.setFilePath(path);
            fileChunkCheckDTO = uploadBlockService.checkChunkExist(chunkDTO);
            return RestApiResponse.success(fileChunkCheckDTO);
        } catch (Exception e) {
            return RestApiResponse.error(e.getMessage());
        }
    }

    /**
     * 上传文件分片
     *
     * @param chunkDTO
     * @return
     */
    @PostMapping("chunk")
    public RestApiResponse<Object> uploadChunk(FileChunkDTO chunkDTO) {
        try {
            uploadBlockService.uploadChunk(chunkDTO);
            return RestApiResponse.success(chunkDTO.getIdentifier());
        } catch (Exception e) {
            return RestApiResponse.error(e.getMessage());
        }
    }


    /**
     * 请求合并文件分片
     *
     * @param chunkDTO
     * @return
     */
    @PostMapping("merge")
    public RestApiResponse<Object> mergeChunks(@RequestBody FileChunkDTO chunkDTO) {
        try {
            String savePath = path+chunkDTO.getType()+ File.separator;
            if(chunkDTO.getType().equals("2")){
                String userParams = SecurityContextHolder.getContext().getAuthentication().getName();
                String user_id = userParams.split(":")[1];
                savePath = savePath+user_id+File.separator;
            }
            String suffix  = chunkDTO.getFilename().substring(chunkDTO.getFilename().lastIndexOf(".")+1);
            String filename = chunkDTO.getIdentifier()+"."+suffix;
            boolean success = uploadBlockService.mergeChunk(chunkDTO.getIdentifier(), filename, chunkDTO.getTotalChunks(),savePath);
            if(success){
                DFile dfile = new DFile();
                dfile.setType(chunkDTO.getType());
                dfile.setClassify_id(chunkDTO.getClassifyId());
                dfile.setName(chunkDTO.getFilename());
                dfile.setFile_name(filename);
                dfile.setFile_path(savePath+filename);
                dfile.setFile_type(chunkDTO.getFileType());
                dfile.setFile_size(chunkDTO.getTotalSize()+"");
                dfile.setFile_suffix(suffix);
                dfile.setIs_share("0");
                dfile.setOrder_by("0");
                dfile.setFile_hash(chunkDTO.getIdentifier());
                String id = GuidUtil.getUuid();
                dfile.setId(id);
                String time = DateTimeUtil.getDateTimeStr();
                dfile.setCreate_time(time);
                //处理数据权限
                String authParams = SecurityContextHolder.getContext().getAuthentication().getName();
                dfile.setCreate_user(authParams.split(":")[1]);
                dfile.setCreate_organize(authParams.split(":")[2]);
                this.dFileService.save(dfile);

            }
            return RestApiResponse.flag(success);
        } catch (Exception e) {
            return RestApiResponse.error(e.getMessage());
        }
    }


    @PostMapping("/downloadFile")
    public void downloadFile(@RequestBody DFile dFile,HttpServletResponse response) throws Exception {
        uploadBlockService.downloadFile(dFile,response);
    }



}
