package zysy.iflytek.aishua.common.interceptor;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import zysy.iflytek.aishua.common.context.UserContext;
import zysy.iflytek.aishua.common.result.Result;
import zysy.iflytek.aishua.common.security.JwtService;
import zysy.iflytek.aishua.config.properties.JwtProperties;

import java.io.PrintWriter;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/user/login",
            "/api/user/register"
    );

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public AuthInterceptor(JwtService jwtService, JwtProperties jwtProperties) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestUri = request.getRequestURI();
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (requestUri.startsWith(prefix)) {
                return true;
            }
        }

        if (!requestUri.startsWith("/api/")) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getTokenHeader());
        String tokenPrefix = jwtProperties.getTokenPrefix();
        if (token == null || !token.startsWith(tokenPrefix)) {
            writeUnauth(response, "请先登录");
            return false;
        }

        String realToken = token.substring(tokenPrefix.length());
        if (!jwtService.validateToken(realToken)) {
            writeUnauth(response, "登录态无效或已过期，请重新登录");
            return false;
        }

        Long userId = jwtService.getUserIdFromToken(realToken);
        if (userId == null) {
            writeUnauth(response, "登录态无效，请重新登录");
            return false;
        }

        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauth(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(Result.unauth(message)));
        }
    }
}
