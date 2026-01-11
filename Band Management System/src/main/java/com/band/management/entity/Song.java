package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 歌曲实体类
 * 
 * @author Band Management Team
 */
@Data
public class Song implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 歌曲ID
     */
    private Long songId;

    /**
     * 专辑ID
     */
    private Long albumId;

    /**
     * 歌曲名称
     */
    private String title;

    /**
     * 词作者
     */
    private String lyricist;

    /**
     * 曲作者
     */
    private String composer;

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
     * 专辑名称（非数据库字段，用于显示）
     */
    private String albumTitle;

    /**
     * 乐队名称（非数据库字段，用于显示）
     */
    private String bandName;
}
