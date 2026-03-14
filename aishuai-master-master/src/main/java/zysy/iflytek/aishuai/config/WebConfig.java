package zysy.iflytek.aishuai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import zysy.iflytek.aishuai.common.interceptor.AuthInterceptor;

/**
 * Web配置：跨域 + 拦截器注册
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    // 跨域配置（适配前端/Vue/Electron）
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*") // 允许所有源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的请求方法
                .allowedHeaders("*") // 允许所有请求头
                // 当前项目使用 Authorization + localStorage，不依赖 Cookie。
                // 若开启 allowCredentials(true)，浏览器侧将拒绝 `*` 源匹配，导致跨域不稳定。
                .allowCredentials(false)
                .maxAge(3600); // 预检请求缓存时间
    }

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**") // 拦截所有/api请求
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/question/**",
                        "/api/exercise/start"
                ); // 放行匿名可用的刷题链路
    }
}