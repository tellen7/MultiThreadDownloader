package com.laowang.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wangyonghao
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@ComponentScan("com.laowang")
public class Application {


    public static void main(String[] args)
    {
        SpringApplication springApplication = new SpringApplication(Application.class);
        springApplication.run(args);
    }
}
