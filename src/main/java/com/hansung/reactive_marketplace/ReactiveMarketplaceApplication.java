package com.hansung.reactive_marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;

@SpringBootApplication
public class ReactiveMarketplaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReactiveMarketplaceApplication.class, args);
	}

}
