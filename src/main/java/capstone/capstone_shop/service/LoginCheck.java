package capstone.capstone_shop.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginCheck implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        boolean isLoggedIn = session != null && session.getAttribute("loginUser") != null;

        if (isLoggedIn)
            return true;

        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String target = (query == null) ? uri : (uri + "?" + query);

        String redirectURL = URLEncoder.encode(target, StandardCharsets.UTF_8);
        response.sendRedirect("/login?redirectURL=" + redirectURL);

        return false;
    }
}
