package dev.ronin.demo.beerstore.infrastructure;

import dev.ronin.demo.beerstore.BeerStoreApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(BeerStoreApplication.class);
	}

}
