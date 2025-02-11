package dev.ronin.demo.client.infrastructure;

import dev.ronin.demo.client.BeerStoreClientApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(BeerStoreClientApplication.class);
	}

}
