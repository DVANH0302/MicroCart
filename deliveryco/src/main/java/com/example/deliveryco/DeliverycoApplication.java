package com.example.deliveryco;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class DeliverycoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeliverycoApplication.class, args);
	}

}
