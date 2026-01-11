package com.easy.web.service;

import com.easy.web.PageInfo;
import com.easy.web.pojo.Student;

import java.util.List;

public interface IStudentService {
    List<Student> selectAll();
    void deleteById(Integer id);
    void add(Student student);
    void update(Student student);
    Student toUpdate(String id);

    PageInfo selectByPage(int pageNo, int pageSize);
}
