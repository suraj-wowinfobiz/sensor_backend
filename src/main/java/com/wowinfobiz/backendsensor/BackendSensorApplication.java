package com.wowinfobiz.backendsensor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
@EntityScan(basePackages = "com.wowinfobiz")
@ComponentScan(
        basePackages = "com.wowinfobiz",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
@EnableJpaRepositories(basePackages = "com.wowinfobiz")
public class BackendSensorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendSensorApplication.class, args);
    }
}
