package zysy.iflytek.aishua.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import zysy.iflytek.aishua.common.interceptor.AuthInterceptor;

/**
 * 全局配置类，负责跨域规则和鉴权拦截器注册。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;

    /**
     * 构造方法，负责注入依赖组件。
     */
    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * 处理当前业务逻辑。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 仅对后端接口路由应用跨域策略。
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    /**
     * 处理当前业务逻辑。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录和注册接口保持公开，其余接口路由均需要令牌鉴权。
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register"
                );
    }
}
