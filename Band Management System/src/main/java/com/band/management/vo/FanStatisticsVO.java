package com.band.management.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 歌迷统计VO
 * 
 * @author Band Management Team
 */
@Data
public class FanStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总歌迷数
     */
    private Integer totalFans;

    /**
     * 性别分布
     */
    private Map<String, Integer> genderDistribution;

    /**
     * 年龄分布
     */
    private Map<String, Integer> ageDistribution;

    /**
     * 职业分布
     */
    private Map<String, Integer> occupationDistribution;

    /**
     * 学历分布
     */
    private Map<String, Integer> educationDistribution;
}
