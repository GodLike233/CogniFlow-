package com.cogniflow.config;

import com.cogniflow.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) return true;

        Object loginUser = request.getSession().getAttribute("loginUser");

        if (loginUser == null) {
            if (path.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"请先登录\"}");
            } else {
                response.sendRedirect("/login");
            }
            return false;
        }

        if (path.startsWith("/api/admin/") || path.startsWith("/admin")) {
            User user = (User) loginUser;
            if (!"ADMIN".equals(user.getRole())) {
                if (path.startsWith("/api/")) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
                } else {
                    response.sendRedirect("/welcome");
                }
                return false;
            }
        }
        return true;
    }
}