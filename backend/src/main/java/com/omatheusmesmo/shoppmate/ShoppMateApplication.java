package com.omatheusmesmo.shoppmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ShoppMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShoppMateApplication.class, args);
	}

}
