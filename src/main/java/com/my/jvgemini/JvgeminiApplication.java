package com.my.jvgemini;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@ComponentScan("com.my.jvgemini.*")
@PropertySource("classpath:application.yml")
public class JvgeminiApplication {

	public static void main(String[] args) {
		SpringApplication.run(JvgeminiApplication.class, args);
	}

}
