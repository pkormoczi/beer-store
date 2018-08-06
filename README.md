## Beer Store
Beer Store is a simple proof of concept application focusing on the following features:

 - [x] Multi module Maven build with Maven wrapper
 - [x] Spring Boot
 - [x] Domain Driven Design considerations
 - [x] Spring Framework with full java config
 - [x] Spring Data with Hibernate
 - [x] SOAP Endpoints
 - [x] REST Controllers
 - [x] Spring MVC
 - [x] Swagger
 - [ ] Spring Security
 - [x] MapStruct as adapter
 - [ ] Integration tests
 - [ ] Unit tests
 - [ ] Test coverage tool   
 

## TODO

 - [ ] Exception and Error handling and documentation in the REST Controllers
 - [ ] Spring Security for SOAP with roles and basic authentication
 - [ ] Unit test for business
 - [ ] Integration tests for REST and SOAP endpoints
 - [ ] Transaction management refinement
 - [ ] Weblogic 12c deployment
 - [ ] Database migration tooling? flyway /liquibase

##DEV notes
- beer-store-contract is a separate artifact. Need to run mvn clean install first for that module.
- Clover reports: `mvn clean clover:setup test clover:aggregate clover:clover`
- Swagger UI at: `http://localhost:8080/swagger-ui.html`
- Swagger JSON: `http://localhost:8080/v2/api-docs`