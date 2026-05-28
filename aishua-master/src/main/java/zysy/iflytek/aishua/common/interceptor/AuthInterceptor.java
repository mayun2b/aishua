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

/**
 * 接口路由令牌鉴权拦截器。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    // 显式开放的公开接口路径。
    /**
     * 处理当前业务逻辑。
     */
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/user/login",
            "/api/user/register"
    );

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public AuthInterceptor(JwtService jwtService, JwtProperties jwtProperties) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 处理当前业务逻辑。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestUri = request.getRequestURI();
        if (isPublicPath(requestUri)) {
            return true;
        }

        // 非接口路由（如静态资源）跳过鉴权。
        if (!requestUri.startsWith("/api/")) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getTokenHeader());
        String tokenPrefix = jwtProperties.getTokenPrefix();
        if (token == null || !token.startsWith(tokenPrefix)) {
            writeUnauth(response, "Please login first");
            return false;
        }

        String realToken = token.substring(tokenPrefix.length());
        if (!jwtService.validateToken(realToken)) {
            writeUnauth(response, "Login token is invalid or expired");
            return false;
        }

        Long userId = jwtService.getUserIdFromToken(realToken);
        if (userId == null) {
            writeUnauth(response, "Login token is invalid");
            return false;
        }

        UserContext.setUserId(userId);
        UserContext.setAuthorization(realToken);
        return true;
    }

    /**
     * 处理当前业务逻辑。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 处理当前业务逻辑。
     */
    private void writeUnauth(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(Result.unauth(message)));
        }
    }

    /**
     * 判断当前条件是否满足。
     */
    private boolean isPublicPath(String requestUri) {
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (requestUri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
