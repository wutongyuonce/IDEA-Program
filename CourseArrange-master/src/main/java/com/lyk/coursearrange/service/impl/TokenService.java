package com.lyk.coursearrange.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.lyk.coursearrange.entity.Admin;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author: 15760
 * @Date: 2020/3/24
 * @Descripe: 年轻时不懂事，乱写的，什么时候有空了再重构吧~
 */
@Service
public class TokenService {

    /**
     * 验证学生
     */
    public String getToken(Student student) {
        Date start = new Date();
        // 一小时有效时间
        long currentTime = System.currentTimeMillis() + 60* 60 * 500;
        Date end = new Date(currentTime);
        String token = "";

        token = JWT.create().withAudience(student.getId().toString()).withIssuedAt(start).withExpiresAt(end)
                .sign(Algorithm.HMAC256(student.getPassword()));
        return token;
    }

    /**
     * 验证管理员
     */
    public String getToken(Admin admin) {
        Date start = new Date();
        long currentTime = System.currentTimeMillis() + 60* 60 * 500;
        Date end = new Date(currentTime);
        String token = "";

        token = JWT.create().withAudience(admin.getId().toString()).withIssuedAt(start).withExpiresAt(end)
                .sign(Algorithm.HMAC256(admin.getPassword()));
        return token;
    }

    /**
     * 验证讲师
     */
    public String getToken(Teacher teacher) {
        Date start = new Date();
        long currentTime = System.currentTimeMillis() + 60* 60 * 500;
        Date end = new Date(currentTime);
        String token = "";

        token = JWT.create().withAudience(teacher.getId().toString()).withIssuedAt(start).withExpiresAt(end)
                .sign(Algorithm.HMAC256(teacher.getPassword()));
        return token;
    }

}
