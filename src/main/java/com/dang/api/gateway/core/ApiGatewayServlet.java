package com.dang.api.gateway.core;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Description:
 *
 * @Author dangfugui  dangfugui@163.cm
 * @Date Create in 2017/10/31
 */
public class ApiGatewayServlet extends HttpServlet {

    private ApplicationContext context;
    private ApiGatewayHand apiHand;
    @Override
    public void init() throws ServletException{
        super.init();
        context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        apiHand = context.getBean(ApiGatewayHand.class);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException{
        apiHand.handle(request,response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException{
        apiHand.handle(request,response);
    }
}
