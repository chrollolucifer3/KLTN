package com.learning_forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LearningForumApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearningForumApplication.class, args);
	}

}
