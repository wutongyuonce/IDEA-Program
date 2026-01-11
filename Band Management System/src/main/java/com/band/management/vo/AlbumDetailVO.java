package com.band.management.vo;

import com.band.management.entity.Song;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 专辑详情VO（包含歌曲列表）
 * 
 * @author Band Management Team
 */
@Data
public class AlbumDetailVO implements Serializable {

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
     * 乐队名称
     */
    private String bandName;

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
     * 评论数
     */
    private Integer reviewCount;

    /**
     * 歌曲列表
     */
    private List<Song> songs;

    /**
     * 歌曲数量
     */
    private Integer songCount;

    /**
     * 排行榜排名（0表示未上榜）
     */
    private Integer ranking;

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
}
