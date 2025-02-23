package com.hansung.reactive_marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class ReactiveMarketplaceApplication {

	public static void main(String[] args) {
//		BlockHound.builder()
//				.allowBlockingCallsInside("org.thymeleaf.spring6.SpringWebFluxTemplateEngine", "process")
//				.install();

		SpringApplication.run(ReactiveMarketplaceApplication.class, args);
	}

}
