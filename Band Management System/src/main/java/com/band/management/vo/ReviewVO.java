package com.band.management.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 乐评VO（包含歌迷和专辑信息）
 * 
 * @author Band Management Team
 */
@Data
public class ReviewVO implements Serializable {

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
     * 歌迷姓名
     */
    private String fanName;

    /**
     * 专辑ID
     */
    private Long albumId;

    /**
     * 专辑名称
     */
    private String albumTitle;

    /**
     * 乐队ID
     */
    private Long bandId;

    /**
     * 乐队名称
     */
    private String bandName;

    /**
     * 评分
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
}
