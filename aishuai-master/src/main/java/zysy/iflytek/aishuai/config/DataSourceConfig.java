package zysy.iflytek.aishuai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 多数据源配置类
 * 配置MySQL主数据源和PostgreSQL向量数据源
 */
@Configuration
public class DataSourceConfig {

    /**
     * 配置MySQL主数据源（存储业务数据）
     */
    @Bean(name = "primaryDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 配置PostgreSQL向量数据源（用于向量化分析）
     */
    @Bean(name = "vectorDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.vector")
    public DataSource vectorDataSource() {
        return DataSourceBuilder.create().build();
    }
}
