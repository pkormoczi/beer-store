package dev.ronin.demo.beerstore.infrastructure.controller;


import dev.ronin.demo.beerstore.infrastructure.security.Authorized;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;

@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class HomeController {

    @Operation(description = "Returns a simple \"Hello!\"")
    @GetMapping(value = "/")
    public String hello() {
        return "Hello!";
    }

    @Authorized
    @GetMapping(value = "/",params = "name")
    public String hello(@RequestParam final String name) {
        return format("Hello %s!",name);
    }
}
