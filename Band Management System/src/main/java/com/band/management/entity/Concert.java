package com.band.management.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 * 演唱会实体类
 * 
 * @author Band Management Team
 */
@Data
public class Concert implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 演唱会ID
     */
    private Long concertId;

    /**
     * 乐队ID
     */
    private Long bandId;

    /**
     * 演唱会名称
     */
    @NotBlank(message = "演唱会名称不能为空")
    @Size(max = 200, message = "演唱会名称不能超过200个字符")
    private String title;

    /**
     * 演出时间
     */
    @NotNull(message = "演出时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date eventTime;

    /**
     * 演出地点
     */
    @NotBlank(message = "演出地点不能为空")
    @Size(max = 200, message = "演出地点不能超过200个字符")
    private String location;

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
     * 参与人数（非数据库字段，用于显示）
     */
    private Integer attendanceCount;
}
