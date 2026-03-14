package zysy.iflytek.aishuai.config;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 配置类
 * 解决 Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required 错误
 */
@Configuration
public class MybatisPlusConfig {

    @Autowired
    private DataSource dataSource;

    /**
     * 配置 SqlSessionFactory
     * 这是 MyBatis Mapper 所需的核心 Bean
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // 配置分页插件
        MybatisPlusInterceptor plusInterceptor = new MybatisPlusInterceptor();
        plusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        factoryBean.setPlugins(plusInterceptor);
        
        // 配置 MyBatis 设置
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true); // 开启驼峰命名转换
        factoryBean.setConfiguration(configuration);
        
        // 如果有 XML 映射文件，取消下面代码的注释
        // PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // factoryBean.setMapperLocations(resolver.getResources("classpath:mapper/**/*.xml"));
        
        return factoryBean.getObject();
    }

    /**
     * 配置 SqlSessionTemplate
     * 这是可选的，但推荐配置以提高性能
     */
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
