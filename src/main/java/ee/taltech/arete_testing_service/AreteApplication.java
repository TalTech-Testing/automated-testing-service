package ee.taltech.arete_testing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication
public class AreteApplication {

	public static void main(String[] args) {
		SpringApplication.run(AreteApplication.class, args);
	}

}
