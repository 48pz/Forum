package com.flow.forum.controller.Interceptor;


import com.flow.forum.entity.LoginTicket;
import com.flow.forum.entity.User;
import com.flow.forum.service.UserService;
import com.flow.forum.util.CookieUtil;
import com.flow.forum.util.HostHolder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            //get login ticket
            LoginTicket loginTicket = userService.getLoginTicket(ticket);
            //check expiration date
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after((new Date()))) {
                //query user by ticket
                User user = userService.findUserById(loginTicket.getUserId());
                //store user in the thread local
                hostHolder.setUser(user);
            }

        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}
