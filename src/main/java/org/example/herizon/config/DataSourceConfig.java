package org.example.herizon.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源配置类
 *
 * 解决MySQL版本兼容性问题：
 * - 某些MySQL版本（如TencentDB CynosDB）不支持tx_read_only系统变量
 * - HikariCP默认会检查连接的只读状态，导致SQLException
 *
 * 解决方案：
 * - 禁用HikariCP的只读检查
 * - 配置兼容的连接属性
 *
 * @author Claude Code
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * 配置自定义数据源，解决tx_read_only兼容性问题
     *
     * @return HikariDataSource实例
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        // 基本连接信息
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        // 连接池配置
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // 关键配置：禁用只读检查
        config.setReadOnly(false);

        // MySQL兼容性配置
        config.addDataSourceProperty("useInformationSchema", "true");
        config.addDataSourceProperty("nullCatalogMeansCurrent", "true");
        config.addDataSourceProperty("useServerPrepStmts", "false");
        config.addDataSourceProperty("cachePrepStmts", "false");

        // 连接测试查询
        config.setConnectionTestQuery("SELECT 1");

        // 连接初始化SQL（可选）
        config.setConnectionInitSql("SELECT 1");

        return new HikariDataSource(config);
    }
}
