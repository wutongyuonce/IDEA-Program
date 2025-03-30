package com.easy.web.servlet;

import com.easy.web.dao.IStudentDao;
import com.easy.web.dao.impl.StudentDaoImpl;
import com.easy.web.pojo.Student;
import com.easy.web.util.JDBCUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

//http://localhost:8080/StudentJDBC/student
@WebServlet("/student")
public class StudentServlet extends HttpServlet {
    private IStudentDao studentDao = new StudentDaoImpl();

    //默认访问service
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //System.out.println("StudentServlet.service");
        //解决post请求乱码问题
        req.setCharacterEncoding("UTF-8");

        // http://localhost:8080/StudentJDBC/student?method=selectAll
        // http://localhost:8080/StudentJDBC/student?method=deleteById&id=2
        String method = req.getParameter("method");
        if (method == null || method.equals("")) {
            method = "selectAll";
        }
        switch (method) {
            case "selectAll":
                selectAll(req, resp);
                break;
            case "deleteById":
                deleteById(req, resp);
                break;
            case "add":
                add(req, resp);
                break;
            case "toUpdate":
                toUpdate(req, resp);
                break;
            case "update":
                update(req, resp);
                break;
        }
    }

    private void update(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("StudentServlet.update");
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String age = req.getParameter("age");
        String gender = req.getParameter("gender");
        // 修改学生信息需要提供id来执行SQL语句，因此使用需要四个参数的构造方法
        studentDao.update(new Student(Integer.parseInt(id), name, Integer.parseInt(age), gender));

        resp.sendRedirect("/StudentJDBC2/student");
    }

    private void toUpdate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("StudentServlet.toUpdate");
        String id = req.getParameter("id");
        Student student = studentDao.toUpdate(id);

        //把list数据放到req里面
        req.setAttribute("student", student);
        //转发到student_update.jsp页面进行展示
        req.getRequestDispatcher("/student_update.jsp").forward(req, resp);
    }

    private void add(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("StudentServlet.add");
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String age = req.getParameter("age");
        String gender = req.getParameter("gender");
        studentDao.add(new Student(Integer.parseInt(id), name, Integer.parseInt(age), gender));

        resp.sendRedirect(req.getContextPath() + "/student?method=selectAll");
    }

    private void deleteById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("StudentServlet.deleteById");
        String id = req.getParameter("id");

        studentDao.deleteById(Integer.parseInt(id));
        // /student   302
        // 重定向
        resp.sendRedirect(req.getContextPath() + "/student?method=selectAll");
    }

    private void selectAll(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("StudentServlet.selectAll");
        List<Student> list = studentDao.selectAll();

        //把list数据放到req里面
        req.setAttribute("list", list);
        //转发到student_list.jsp页面进行展示
        req.getRequestDispatcher("/student_list.jsp").forward(req, resp);
    }
}
