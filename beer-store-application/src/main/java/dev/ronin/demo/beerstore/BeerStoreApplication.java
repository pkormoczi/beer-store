package dev.ronin.demo.beerstore;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@SpringBootApplication
@Slf4j
public class BeerStoreApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BeerStoreApplication.class);
        addInitHooks(application);
        application.run(args);
    }

    static void addInitHooks(SpringApplication application) {
        application.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
            Environment environment = event.getEnvironment();
            String activeProfiles = Arrays.toString(environment.getActiveProfiles());
            String datasource = environment.getProperty("spring.datasource.url");
            log.info("--------------------------ApplicationListener#ApplicationEnvironmentPreparedEvent()--------------------------");
            log.info(String.format("--------------------------Active Profiles = %s--------------------------", activeProfiles));
            log.info(String.format("--------------------------Datasource URL = %s--------------------------", datasource));
        });
        application.addListeners((ApplicationListener<ApplicationPreparedEvent>) event -> {
            Environment environment = event.getApplicationContext().getEnvironment();
            String activeProfiles = Arrays.toString(environment.getActiveProfiles());
            String datasource = environment.getProperty("spring.datasource.url");
            log.info("--------------------------ApplicationListener#ApplicationPreparedEvent()--------------------------");
            log.info(String.format("--------------------------Active Profiles = %s--------------------------", activeProfiles));
            log.info(String.format("--------------------------Datasource URL = %s--------------------------", datasource));
//            if (!checkJdbUrl(datasource)){
//                throw new ApplicationContextException("Invalid Datasource URL!");
//            }
        });
    }

    static boolean checkJdbUrl(String url){
        Predicate<String> predicate = Pattern.compile("^jdbc:(oracle|h2):(thin|mem):([a-zA-Z0-9-.]*)(:[0-9]+)?;?.*$").asPredicate();
        return predicate.test(url);
    }
}
