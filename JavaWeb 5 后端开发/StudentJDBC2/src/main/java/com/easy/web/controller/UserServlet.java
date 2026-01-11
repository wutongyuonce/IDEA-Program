package com.easy.web.controller;

import com.easy.web.pojo.User;
import com.easy.web.service.IUserService;
import com.easy.web.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/user")
public class UserServlet extends HttpServlet {
    IUserService userService = new UserServiceImpl();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("UserServlet.service");
//        HttpSession session = req.getSession();
//        User user = (User) session.getAttribute("user");
//        if(user == null){
//            resp.sendRedirect("login.jsp");
//            return;
//        }

        String method = req.getParameter("method");
        if (method == null || method.equals("")) {
            method = "update";
        }
        switch (method) {
            case "login":
                login(req, resp);
                break;
            case "logout":
                logout(req, resp);
                break;
            case "update":
                update(req, resp);
                break;
        }
    }

    private void update(HttpServletRequest req, HttpServletResponse resp) {
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("UserServlet.logout");
        HttpSession session = req.getSession();
        session.removeAttribute("user");
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("UserServlet.login");

        // 1. 优先检查是否有表单提交的账号密码
        String name = req.getParameter("name");
        String password = req.getParameter("password");

        // 如果用户提交了表单，直接验证新账号密码
        if (name != null && !name.isEmpty() && password != null && !password.isEmpty()) {
            System.out.println("表单提交的账号密码: " + name + ";" + password);
            login2(req, resp, name, password);
            return;
        }

        // 2. 再检查 Session 的自动登录
        HttpSession session = req.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                System.out.println("通过 Session 自动登录: " + user.getName());
                login2(req, resp, user.getName(), user.getPassword());
                return;
            }
        }

        // 3. 如果以上都没有，重定向到登录页
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    private void login2(HttpServletRequest req, HttpServletResponse resp, String name, String password) throws IOException {
        System.out.println("UserServlet.login2");
        User user = userService.login(name, password);
        if (user != null) {
            // 登录成功，存储 Session
            HttpSession session = req.getSession();
            session.setAttribute("user", user);

            // 如果用户勾选“记住用户名”，存储Cookie
            String remember = req.getParameter("remember");
            if ("true".equals(remember)) {
                Cookie nameCookie = new Cookie("rememberedName", name);
                nameCookie.setMaxAge(7 * 24 * 60 * 60); // 7天有效期
                resp.addCookie(nameCookie);
            } else {
                // 清除旧的 Cookie
                Cookie[] cookies = req.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("rememberedName".equals(cookie.getName())) {
                            cookie.setMaxAge(0);
                            resp.addCookie(cookie);
                        }
                    }
                }
            }

            resp.sendRedirect(req.getContextPath() + "/");
        } else {
            resp.sendRedirect(req.getContextPath() + "/fail.jsp");
        }
    }
}
