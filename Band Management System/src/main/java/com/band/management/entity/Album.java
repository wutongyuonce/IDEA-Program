package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 专辑实体类
 * 
 * @author Band Management Team
 */
@Data
public class Album implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 专辑ID
     */
    private Long albumId;

    /**
     * 乐队ID
     */
    private Long bandId;

    /**
     * 专辑名称
     */
    private String title;

    /**
     * 发行日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date releaseDate;

    /**
     * 专辑文案
     */
    private String copywriting;

    /**
     * 平均评分
     */
    private BigDecimal avgScore;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;

    /**
     * 乐队名称（非数据库字段，用于显示）
     */
    private String bandName;

    /**
     * 是否已收藏（非数据库字段，用于前端显示）
     */
    @com.fasterxml.jackson.annotation.JsonProperty("isFavorited")
    private Boolean isFavorited;
}
