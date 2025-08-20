package org.scit4bits.tonarinetserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TonarinetserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(TonarinetserverApplication.class, args);
	}

}
