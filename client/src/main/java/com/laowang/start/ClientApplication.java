package com.laowang.start;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * @author wangyonghao
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@ComponentScan("com.laowang")
public class ClientApplication {

    @Autowired
    private RestTemplateBuilder builder;

    public static void main(String[] args)
    {
        SpringApplication springApplication = new SpringApplication(ClientApplication.class);
        springApplication.run(args);
    }

    @Bean(name = "restTemplate")
    public RestTemplate getRestTemplate(){
        return builder.build();
    }

}
