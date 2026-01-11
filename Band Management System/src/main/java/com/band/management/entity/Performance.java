package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 乐队演唱歌曲关系实体类
 * 
 * @author Band Management Team
 */
@Data
public class Performance implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 演唱关系ID
     */
    private Long perfId;

    /**
     * 演唱乐队ID
     */
    private Long bandId;

    /**
     * 演唱歌曲ID
     */
    private Long songId;

    /**
     * 原创乐队ID (NULL表示翻唱或未知)
     */
    private Long sourceBandId;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}
