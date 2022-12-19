package com.qingfeng.common.service;


import com.qingfeng.common.dto.FileChunkDTO;
import com.qingfeng.common.dto.FileChunkResultDTO;
import com.qingfeng.entity.document.DFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ProjectName IUploadBlockService
 * @author Administrator
 * @version 1.0.0
 * @Description 附件分片上传
 * @createTime 2022/4/13 0013 15:57
 */
public interface IUploadBlockService {

    /**
     * 检查文件是否存在，如果存在则跳过该文件的上传，如果不存在，返回需要上传的分片集合
     * @param chunkDTO
     * @return
     */
    FileChunkResultDTO checkChunkExist(FileChunkDTO chunkDTO);


    /**
     * 上传文件分片
     * @param chunkDTO
     */
    void uploadChunk(FileChunkDTO chunkDTO) throws IOException;


    /**
     * 合并文件分片
     * @param identifier
     * @param fileName
     * @param totalChunks
     * @return
     * @throws IOException
     */
    boolean mergeChunk(String identifier,String fileName,Integer totalChunks,String path)throws IOException;


    public void downloadFile(DFile dFile, HttpServletResponse response) throws Exception ;

}
