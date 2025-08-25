package capstone.capstone_shop.service;

import capstone.capstone_shop.domain.UserRole;
import capstone.capstone_shop.dto.LoginUserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class AdminCheck implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        LoginUserDto login = (session == null) ? null : (LoginUserDto) session.getAttribute("loginUser");

        if (login == null){
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            String target = (query == null) ? uri : (uri + "?" + query);
            String redirectURL = URLEncoder.encode(target, StandardCharsets.UTF_8);

            response.sendRedirect("/login?redirectURL=" + redirectURL);
            return false;
        }

        if (login.userRole() != UserRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}
