package com.qingfeng.common.dto;

import java.util.Set;

/**
 * @ProjectName FileChunkResultDTO
 * @author Administrator
 * @version 1.0.0
 * @Description 文件分片
 * @createTime 2022/4/13 0013 15:58
 */
public class FileChunkResultDTO {
    /**
     * 是否跳过上传
     */
    private Boolean skipUpload;

    /**
     * 已上传分片的集合
     */
    private Set<Integer> uploaded;

    public Boolean getSkipUpload() {
        return skipUpload;
    }

    public void setSkipUpload(Boolean skipUpload) {
        this.skipUpload = skipUpload;
    }

    public Set<Integer> getUploaded() {
        return uploaded;
    }

    public void setUploaded(Set<Integer> uploaded) {
        this.uploaded = uploaded;
    }


    public FileChunkResultDTO(Boolean skipUpload, Set<Integer> uploaded) {
        this.skipUpload = skipUpload;
        this.uploaded = uploaded;
    }

    public FileChunkResultDTO(Boolean skipUpload) {
        this.skipUpload = skipUpload;
    }
}
