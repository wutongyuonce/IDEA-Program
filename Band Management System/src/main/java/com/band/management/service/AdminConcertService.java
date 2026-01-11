package com.band.management.service;

import com.band.management.common.PageResult;
import com.band.management.entity.Concert;
import java.util.List;

public interface AdminConcertService {
    Long create(Concert concert);
    void delete(Long concertId);
    void update(Concert concert);
    Concert getById(Long concertId);
    List<Concert> getByBandId(Long bandId);
    PageResult<Concert> page(int pageNum, int pageSize, Concert condition);
    List<Concert> list();
}
