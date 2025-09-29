package org.example.herizon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Herizon女性职业赋能社区平台主启动类
 * <p>
 * 技术栈：
 * - Spring Boot 3.5.6 + Java 21
 * - MyBatis-Flex ORM框架
 * - MySQL数据库
 * - 逻辑删除支持
 * <p>
 * 服务器配置：
 * - 端口：8080
 * - 上下文路径：/api
 *
 * @author Kokoa
 * @version 1.0
 */
@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration.class
})
@MapperScan ("org.example.herizon.mapper") // 扫描MyBatis-Flex Mapper接口
public class Application{

    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }

}
