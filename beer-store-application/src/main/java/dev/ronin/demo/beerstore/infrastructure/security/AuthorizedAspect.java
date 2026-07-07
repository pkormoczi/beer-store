package dev.ronin.demo.beerstore.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class AuthorizedAspect {

    @Before("@annotation(dev.ronin.demo.beerstore.infrastructure.security.Authorized)")
    public void validate(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Authorized authorized = signature.getMethod().getAnnotation(Authorized.class);
        String requiredAuthority = "ROLE_" + authorized.value();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean hasRequiredRole = authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(requiredAuthority::equals);

        if (!hasRequiredRole) {
            throw new AuthorizationException(
                    "%s requires role %s".formatted(signature.toShortString(), authorized.value()));
        }
    }
}
