package dev.ronin.demo.beerstore.platform.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import static dev.ronin.demo.beerstore.BeerStoreApplication.PROFILE_TEST;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@Profile("!" + PROFILE_TEST)
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/services/**")).hasRole("USER")
                                .anyRequest().hasRole("USER"))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // A stateless, HTTP-Basic-only API has no browser session/cookie for CSRF to
                // protect; leaving CSRF enabled here was never exercised while /** was ignored,
                // but with the filter chain actually reachable it rejects every non-GET request
                // as anonymous+401 before Basic auth even runs (CsrfFilter precedes
                // BasicAuthenticationFilter in the chain).
                .csrf(csrf -> csrf.disable())
                .httpBasic(withDefaults())
                .build();
    }

    /**
     * Only genuinely public, static documentation resources are exempt from the filter chain.
     * Previously this ignored {@code /**} - i.e. every request, including the REST/SOAP API
     * the filter chain above claims to protect - which made {@link #securityFilterChain} a no-op.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui/**"))
                .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui.html"))
                .requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/v3/api-docs/**"));
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // {noop} is the demo-appropriate choice here: an in-memory user store with no
        // PasswordEncoder bean would otherwise fail every login ("no PasswordEncoder mapped
        // for the id null"), since Spring Security's default DelegatingPasswordEncoder requires
        // an {id} prefix on stored passwords.
        UserDetails user = User.builder()
                .username("user")
                .password("{noop}password")
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}admin")
                .roles("USER", "ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user, admin);
    }
}
