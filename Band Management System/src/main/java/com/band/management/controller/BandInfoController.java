package com.band.management.controller;

import com.band.management.common.Result;
import com.band.management.entity.Band;
import com.band.management.service.BandInfoService;
import com.band.management.vo.BandVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 乐队信息控制器
 * 
 * @author Band Management Team
 */
@Slf4j
@RestController
@RequestMapping("/api/band/info")
public class BandInfoController {

    @Autowired
    private BandInfoService bandInfoService;

    /**
     * 获取当前乐队信息
     */
    @GetMapping("/{bandId}")
    public Result<Band> getCurrentBandInfo(@PathVariable("bandId") Long bandId) {
        log.info("乐队查询自己的信息: {}", bandId);
        Band band = bandInfoService.getCurrentBandInfo(bandId);
        return Result.success(band);
    }

    /**
     * 获取当前乐队详细信息（包含成员列表）
     */
    @GetMapping("/{bandId}/detail")
    public Result<BandVO> getCurrentBandDetail(@PathVariable("bandId") Long bandId) {
        log.info("乐队查询自己的详细信息: {}", bandId);
        BandVO bandVO = bandInfoService.getCurrentBandDetail(bandId);
        return Result.success(bandVO);
    }

    /**
     * 更新乐队信息
     */
    @PutMapping("/{bandId}")
    public Result<Void> updateBandInfo(
            @PathVariable("bandId") Long bandId,
            @RequestBody @Valid Band band) {
        log.info("乐队更新自己的信息: {}", bandId);
        bandInfoService.updateBandInfo(bandId, band);
        return Result.success("更新成功");
    }

    /**
     * 解散乐队
     */
    @PutMapping("/{bandId}/disband")
    public Result<Void> disbandBand(@PathVariable("bandId") Long bandId) {
        log.info("乐队解散自己: {}", bandId);
        bandInfoService.disbandBand(bandId);
        return Result.success("解散乐队成功");
    }
}
