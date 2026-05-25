package zysy.iflytek.aishua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class AishuaApplication {
    public static void main(String[] args) {
        SpringApplication.run(AishuaApplication.class, args);
    }
}
