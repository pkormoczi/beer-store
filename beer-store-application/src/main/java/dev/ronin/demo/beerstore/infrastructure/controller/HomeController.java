package dev.ronin.demo.beerstore.infrastructure.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;

@Api(value = "HomeAPI")
@RestController
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class HomeController {

    @ApiOperation(value = "Simple Hello World message", response = String.class)
    @GetMapping(value = "/")
    public String hello() {
        return "Hello!";
    }

    @ApiOperation(value = "Simple Hello World message with Name parameter", response = String.class)
    @GetMapping(value = "/",params = "name")
    public String hello(@ApiParam("Name") @RequestParam final String name) {
        return format("Hello %s!",name);
    }
}
