package com.easy.web.service.impl;

import com.easy.web.PageInfo;
import com.easy.web.dao.IStudentDao;
import com.easy.web.dao.ITeacherDao;
import com.easy.web.dao.impl.StudentDaoImpl;
import com.easy.web.dao.impl.TeacherDaoImpl;
import com.easy.web.pojo.Student;
import com.easy.web.service.IStudentService;

import java.util.List;

public class StudentServiceImpl implements IStudentService {
    private IStudentDao studentDao = new StudentDaoImpl();
    private ITeacherDao teacherDao = new TeacherDaoImpl();

    @Override
    public List<Student> selectAll() {
        return studentDao.selectAll();
    }

    @Override
    public void deleteById(Integer id) {
        studentDao.deleteById(id);
    }

    @Override
    public void add(Student student) {
        studentDao.add(student);
    }

    @Override
    public void update(Student student) {
        studentDao.update(student);
    }

    @Override
    public Student toUpdate(String id) {
        return studentDao.toUpdate(id);
    }

    @Override
    public PageInfo selectByPage(int pageNo, int pageSize) {
        // 第一条sql，查询当前页数据
        int offset = (pageNo - 1) * pageSize;
        List<Student> studentList = studentDao.selectPage(offset, pageSize);

        // 第二条sql，查询总的数量
        int totalCount = studentDao.selectTotalCount();
        int totalPage = (int)Math.ceil((double)totalCount / pageSize);
        PageInfo pageInfo = new PageInfo(studentList, totalPage, pageNo, pageSize);
        System.out.println(pageInfo);
        return pageInfo;
    }
}
