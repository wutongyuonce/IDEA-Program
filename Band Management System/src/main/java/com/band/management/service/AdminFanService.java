package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Fan;
import java.util.List;

public interface AdminFanService {
    Long create(Fan fan);
    void delete(Long fanId);
    void update(Fan fan);
    Fan getById(Long fanId);
    PageResult<Fan> page(int pageNum, int pageSize, Fan condition);
    List<Fan> list();
}
