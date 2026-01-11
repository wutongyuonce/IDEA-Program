package com.easy.web.filter;

import com.easy.web.pojo.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/*
 * 用来进行用户登录验证的过滤器
*
对于某些资源,必须用户登录之后才可以访问;
 */
@WebFilter(filterName = "loginFilter",urlPatterns = "/*")
public class LoginFilter implements Filter {
    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        // 1.获取HttpServletRequest与HttpServletResponse
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        // 2.login请求和一些静态资源不需要判断是否已经登陆
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();
        String path = uri.substring(contextPath.length());

        System.out.println("path: " + path);
        // path：/login.jsp
        // path：/static/layui/layui.js
        // path: /user?method=login
        String method = request.getParameter("method");
        // 注意空指针null，切换比较顺序来解决
        if(path.startsWith("/static")
                || path.equals("/login.jsp")
                || path.equals("/fail.jsp")
                || path.equals("/user") && "login".equals(method)) {
            chain.doFilter(req, resp);
            return;
        }

        // 3.获取Session对象
        HttpSession session = request.getSession();
        // 4.从Session对象获取登录信息(一般情况下为用户对象)
        User user = (User) session.getAttribute("user");
        // 5.判断登录信息对象是否为null
        if (user == null) {
            // 未登录
            // 响应重定向到登录页面
            ((HttpServletResponse) resp).sendRedirect(((HttpServletRequest) req).getContextPath() + "/login.jsp");
        } else {
            //已登录，直接放行
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}
