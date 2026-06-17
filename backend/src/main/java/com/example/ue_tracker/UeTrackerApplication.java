package com.example.ue_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UeTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(UeTrackerApplication.class, args);
	}

}
