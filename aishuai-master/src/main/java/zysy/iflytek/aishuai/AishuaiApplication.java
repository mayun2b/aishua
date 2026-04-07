package zysy.iflytek.aishuai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("zysy.iflytek.aishuai.*.mapper") // 扫描所有模块的 Mapper
public class AishuaiApplication {
    public static void main(String[] args) {
        SpringApplication.run(AishuaiApplication.class, args);
    }
}