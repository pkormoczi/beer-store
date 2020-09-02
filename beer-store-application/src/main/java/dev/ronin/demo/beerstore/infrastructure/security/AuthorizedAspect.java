package dev.ronin.demo.beerstore.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AuthorizedAspect {

    @Before("@annotation(dev.ronin.demo.beerstore.infrastructure.security.Authorized) && args(name,..)")
    public void validate(String name) {
        if (name.equalsIgnoreCase("anyád")) throw new AuthorizationException(String.format("Authorization error! %s is forbidden!", name));
    }

}