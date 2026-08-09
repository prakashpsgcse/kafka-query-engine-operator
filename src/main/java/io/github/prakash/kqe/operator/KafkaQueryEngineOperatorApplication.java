package io.github.prakash.kqe.operator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KafkaQueryEngineOperatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaQueryEngineOperatorApplication.class, args);
	}

}
