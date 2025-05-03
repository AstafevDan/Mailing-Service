package com.dan.mailingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MailingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailingServiceApplication.class, args);
    }

}
