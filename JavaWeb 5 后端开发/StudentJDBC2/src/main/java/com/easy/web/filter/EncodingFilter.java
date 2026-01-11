package com.easy.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;

import java.io.IOException;

/*
 * 字符编码过滤器
 */
@WebFilter(
        filterName = "encodingFilter",
        urlPatterns = "/*",
        initParams = {
                @WebInitParam(name = "encoding", value = "utf-8")
        }
)
public class EncodingFilter implements Filter {
    private String encoding;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //对于所有请求和响应设置字符编码
        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);
        response.setContentType("text/html;charset=" + encoding);
        //继续向后执行
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //获取web.xml或注解里面配置的字符编码过滤器的初始化参数
        encoding = filterConfig.getInitParameter("encoding");
    }
}
