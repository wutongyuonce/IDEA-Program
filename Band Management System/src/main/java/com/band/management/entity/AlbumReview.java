package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 专辑乐评实体类
 * 
 * @author Band Management Team
 */
@Data
public class AlbumReview implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 乐评ID
     */
    private Long reviewId;

    /**
     * 歌迷ID
     */
    private Long fanId;

    /**
     * 专辑ID
     */
    private Long albumId;

    /**
     * 评分 (1-10，步长0.5)
     */
    private BigDecimal rating;

    /**
     * 评论内容
     */
    private String comment;

    /**
     * 评论时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewedAt;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 歌迷姓名（非数据库字段，用于显示和查询）
     */
    private String fanName;

    /**
     * 专辑名称（非数据库字段，用于显示和查询）
     */
    private String albumTitle;

    /**
     * 乐队名称（非数据库字段，用于显示）
     */
    private String bandName;
}
