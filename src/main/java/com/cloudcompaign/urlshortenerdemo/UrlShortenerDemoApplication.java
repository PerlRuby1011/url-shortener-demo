package com.cloudcompaign.urlshortenerdemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.cloudcompaign"})
public class UrlShortenerDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrlShortenerDemoApplication.class, args);
	}

}
