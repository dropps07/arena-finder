package com.arenafinder.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Auth Service.
 *
 * @SpringBootApplication is shorthand for three annotations:
 * @Configuration — this class can define Spring beans
 * @EnableAutoConfiguration — Spring Boot auto-configures based on
 *                          what's on the classpath (e.g. sees
 *                          PostgreSQL driver → sets up DataSource)
 * @ComponentScan — scans this package and sub-packages for
 *                @Component, @Service, @Repository, @Controller
 *
 *                This is intentionally minimal. No logic lives here.
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
