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
 * API 鉴权拦截器。
 * 职责：校验 JWT，解析 userId，并写入 UserContext 供后续业务层使用。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    // 无需登录即可访问的接口前缀
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/user/login",
            "/api/user/register"
    );

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    /**
     * 注入 JWT 服务与配置。
     */
    public AuthInterceptor(JwtService jwtService, JwtProperties jwtProperties) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * 请求进入 Controller 前执行鉴权。
     * 仅拦截 /api/**，并排除登录注册接口。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String requestUri = request.getRequestURI();
        // 公开接口放行
        if (isPublicPath(requestUri)) {
            return true;
        }

        // 非接口路由（如静态资源）跳过鉴权。
        if (!requestUri.startsWith("/api/")) {
            return true;
        }

        String token = request.getHeader(jwtProperties.getTokenHeader());
        String tokenPrefix = jwtProperties.getTokenPrefix();
        // 请求头格式必须是 "Bearer <token>"
        if (token == null || !token.startsWith(tokenPrefix)) {
            writeUnauth(response, "Please login first");
            return false;
        }

        // 去掉 Bearer 前缀后校验 token
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
     * 请求完成后清理线程上下文，避免线程复用造成用户信息串线。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 统一输出 401 响应体。
     */
    private void writeUnauth(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(Result.unauth(message)));
        }
    }

    /**
     * 判断当前请求是否属于公开接口。
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
