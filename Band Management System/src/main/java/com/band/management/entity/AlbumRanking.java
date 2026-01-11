package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 专辑排行榜实体类
 * 
 * @author Band Management Team
 */
@Data
public class AlbumRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排行榜ID
     */
    private Integer rankingId;

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
    private String albumTitle;

    /**
     * 乐队名称
     */
    private String bandName;

    /**
     * 平均评分
     */
    private BigDecimal avgScore;

    /**
     * 评论数
     */
    private Integer reviewCount;

    /**
     * 发行日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date releaseDate;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}
