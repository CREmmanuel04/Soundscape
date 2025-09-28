package com.example.soundscape;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.soundscape")
public class SoundscapeApp {

	public static void main(String[] args) {
		SpringApplication.run(SoundscapeApp.class, args);
	}

}
