package com.easy.web.controller;

import com.easy.web.dao.IStudentDao;
import com.easy.web.dao.impl.StudentDaoImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/class")
public class ClassServlet extends HttpServlet {
    // Controller层可以任意调用下面层中的代码，只要使用下层对象即可
    private IStudentDao studentDao = new StudentDaoImpl();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("ClassServlet");
    }
}
