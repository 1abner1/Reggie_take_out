package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//检查用户是否完成登录
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器
    public static  final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse)  servletResponse;
        // 1.获得请求的URI
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);

        //2.检查登入状态
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };

        boolean check =check(urls,requestURI);


        //3.如果不需要出来，则直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }
        //4.判断是否登录
        if (request.getSession().getAttribute("employee") != null ){
            log.info("用户已经登入，用户id:{}",request.getSession().getAttribute("employee"));

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);



            long id = Thread.currentThread().getId();
            log.info("线程id为:{}",id);

            filterChain.doFilter(request,response);
            return;
        }
        log.info("用户未登录");
        //5.如果未登入则返回未登录结果，通过输出流的形式向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


//        log.info("拦截到请求:{}",request.getRequestURI());
//        filterChain.doFilter(request,response);
    }

    public boolean check (String[] urls,String requestURI){
        for (String Url : urls){
            boolean match  = PATH_MATCHER.match(Url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
