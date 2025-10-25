package com.example.hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// JSP + (tomcat(WAS) + Servlet) - 요즘 안씀...
// Spring Model - View - Control(MVC) - template(Thymeleaf, mustache, Jinja...)
// 통합된 구조 --> 소규모 프로젝트, 간단한 웹 어플리케이션 제작시에 사용
// python : django, flask, fastapi

// Back-End
// Full-stack : BE + FE(react, android mobile)

@SpringBootApplication
public class HelloApplication {

	public static void main(String[] args) {

        SpringApplication.run(HelloApplication.class, args);
	}

}
