package org.zjh.aizerocode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("org.zjh.aizerocode.mapper")
@SpringBootApplication
public class AiZeroCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiZeroCodeApplication.class, args);
    }

}
