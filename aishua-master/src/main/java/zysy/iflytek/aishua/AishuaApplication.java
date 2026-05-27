package zysy.iflytek.aishua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用启动入口，负责初始化并启动后端服务。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class AishuaApplication {
    /**
     * 程序入口方法。
     */
    public static void main(String[] args) {
        SpringApplication.run(AishuaApplication.class, args);
    }
}
