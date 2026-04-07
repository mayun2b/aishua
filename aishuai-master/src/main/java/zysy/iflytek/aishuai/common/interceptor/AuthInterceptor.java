package zysy.iflytek.aishuai.common.interceptor;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import zysy.iflytek.aishuai.common.result.Result;
import zysy.iflytek.aishuai.common.constant.JwtConstants;
import zysy.iflytek.aishuai.utils.JwtUtil;

import java.io.PrintWriter;
import java.util.List;

/**
 * 权限拦截器：区分匿名/登录用户
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * 允许匿名访问的接口前缀（其余 /api/** 默认需要登录）
     */
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/user/login",
            "/api/user/register",
            "/api/question/",
            "/api/exercise/start"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放行OPTIONS请求（跨域预检）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String requestURI = request.getRequestURI();
        // 2. 放行匿名接口
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (requestURI.startsWith(prefix)) {
                return true;
            }
        }

        // 3. /api/** 其余接口默认需要登录
        if (!requestURI.startsWith("/api/")) {
            return true;
        }

        String token = request.getHeader(JwtConstants.TOKEN_HEADER);
        // 无Token -> 未登录
        if (token == null || !token.startsWith(JwtConstants.TOKEN_PREFIX)) {
            writeUnauth(response, "请登录后使用该功能");
            return false;
        }

        // 验证Token有效性
        String realToken = token.substring(JwtConstants.TOKEN_PREFIX.length());
        if (!JwtUtil.validateToken(realToken)) {
            writeUnauth(response, "Token失效，请重新登录");
            return false;
        }

        // 解析用户ID，存入请求上下文
        Long userId = JwtUtil.getUserIdFromToken(realToken);
        if (userId == null) {
            writeUnauth(response, "Token无效，请重新登录");
            return false;
        }
        request.setAttribute("userId", userId);
        return true;
    }

    private void writeUnauth(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(JSON.toJSONString(Result.unauth(message)));
        }
    }
}