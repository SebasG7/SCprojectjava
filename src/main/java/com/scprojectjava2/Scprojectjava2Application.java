package com.scprojectjava2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.scprojectjava2")
@EntityScan("com.scprojectjava2.model")
@EnableJpaRepositories("com.scprojectjava2.repository")
public class Scprojectjava2Application {

	public static void main(String[] args) {
		SpringApplication.run(Scprojectjava2Application.class, args);
	}

}
