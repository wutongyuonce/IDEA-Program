package com.easy.web.controller;

import com.easy.web.PageInfo;
import com.easy.web.pojo.Student;
import com.easy.web.service.IStudentService;
import com.easy.web.service.impl.StudentServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

//http://localhost:8080/StudentJDBC2/student
@WebServlet("/student")
public class StudentServlet extends HttpServlet {
    private IStudentService studentService = new StudentServiceImpl();

    //默认访问service
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("StudentServlet.service");
        //解决post请求乱码问题，已通过过滤器实现
        //req.setCharacterEncoding("UTF-8");

        // http://localhost:8080/StudentJDBC2/student?method=selectAll
        // http://localhost:8080/StudentJDBC2/student?method=deleteById&id=2
        String method = req.getParameter("method");
        if (method == null || method.equals("")) {
            method = "selectByPage";
        }
        switch (method) {
            case "selectAll":
                selectAll(req, resp);
                break;
            case "selectByPage":
                selectByPage(req, resp);
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

    // 分页展示数据
    // http://localhost:8080/StudentJDBC2/student?method=selectByPage&pageNo=?&pageSize=?
    private void selectByPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("StudentServlet.selectByPage");
        String pageNo = req.getParameter("pageNo");
        String pageSize = req.getParameter("pageSize");
        if(pageNo == null || pageNo.equals("")) {
            pageNo = "1";
        }
        if(pageSize == null || pageSize.equals("")) {
            pageSize = "5";
        }
        PageInfo pageInfo = studentService.selectByPage(Integer.parseInt(pageNo),Integer.parseInt(pageSize));

        req.setAttribute("pageInfo", pageInfo);
        req.getRequestDispatcher("/student_list2.jsp").forward(req,resp);
    }

    private void update(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("StudentServlet.update");
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String age = req.getParameter("age");
        String gender = req.getParameter("gender");
        // 修改学生信息需要提供id来执行SQL语句，因此使用需要四个参数的构造方法
        studentService.update(new Student(Integer.parseInt(id), name, Integer.parseInt(age), gender));

        resp.sendRedirect(req.getContextPath() + "/student");
    }

    private void toUpdate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("StudentServlet.toUpdate");
        String id = req.getParameter("id");
        Student student = studentService.toUpdate(id);

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
        studentService.add(new Student(Integer.parseInt(id), name, Integer.parseInt(age), gender));

        resp.sendRedirect(req.getContextPath() + "/student?method=selectByPage");
    }

    private void deleteById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("StudentServlet.deleteById");
        String id = req.getParameter("id");

        studentService.deleteById(Integer.parseInt(id));
        // /student   302
        // 重定向
        resp.sendRedirect(req.getContextPath() + "/student?method=selectByPage");
    }

    // 单页面展示所有数据
    private void selectAll(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("StudentServlet.selectAll");
        List<Student> list = studentService.selectAll();

        //把list数据放到req里面
        req.setAttribute("list", list);
        //转发到student_list.jsp页面进行展示
        req.getRequestDispatcher("/student_list.jsp").forward(req, resp);
    }
}
