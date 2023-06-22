package dev.ronin.demo.beerstore.infrastructure.logging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.util.*;

import static java.util.Objects.requireNonNull;

@Slf4j
public class PropertiesLogger implements ApplicationListener<ApplicationPreparedEvent> {

    @SuppressWarnings("squid:S2068")
    private static final String PASSWORD_PROPERTY_NAME = "password";

    private ConfigurableEnvironment environment;
    private boolean isFirstRun = true;

    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        log.debug("ApplicationPreparedEvent fired. FirstRun: {}", isFirstRun);
        if (isFirstRun) {
            environment = event.getApplicationContext().getEnvironment();
            printProperties();
        }
        isFirstRun = false;
    }

    @SuppressWarnings("squid:S2639")
    public void printProperties() {
        Map<String, String> allProperties = new HashMap<>();
        log.info("******* APPLICATION PROPERTIES *******");
        for (OriginTrackedMapPropertySource propertySource : findPropertiesPropertySources()) {
            String[] propertyNames = propertySource.getPropertyNames();
            Arrays.sort(propertyNames);
            for (String propertyName : propertyNames) {
                String resolvedProperty = environment.getProperty(propertyName);
                String propertyValueInMap = allProperties.computeIfAbsent(propertyName, propertyValue -> {
                    if (propertyName.contains(PASSWORD_PROPERTY_NAME)) {
                        return requireNonNull(resolvedProperty).replaceAll(".", "*");
                    }
                    return resolvedProperty;
                });

                log.info("{}= {}", propertyName, propertyValueInMap);
            }
        }
    }

    private List<OriginTrackedMapPropertySource> findPropertiesPropertySources() {
        List<OriginTrackedMapPropertySource> propertiesPropertySources = new LinkedList<>();
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            if (propertySource instanceof OriginTrackedMapPropertySource) {
                propertiesPropertySources.add((OriginTrackedMapPropertySource) propertySource);
            }
        }
        return propertiesPropertySources;
    }
}
