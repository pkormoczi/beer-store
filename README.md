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
 - [x] Spring Security
 - [x] MapStruct as adapter
 - [ ] Integration tests
 - [x] Unit tests
 - [x] Test coverage tool   
 

## TODO

 - [x] Exception and Error handling and documentation in the REST Controllers
 - [x] Spring Security for SOAP with roles and basic authentication
 - [ ] Profiling - local, integration test, docker, prod
 - [ ] Unit test for business
 - [ ] Integration tests for REST and SOAP endpoints
 - [ ] Separate unit and integration tests in jenkins pipeline
 - [ ] Transaction management refinement
 - [ ] Weblogic 12c deployment
 - [ ] Database migration tooling? flyway /liquibase
 - [ ] Async calls via SOAP
 - [ ] Events, with Spring application events

##DEV notes
- beer-store-contract is a separate artifact. Need to run mvn clean install first for that module.
- Clover reports: `mvn clean clover:setup test clover:aggregate clover:clover`
- Swagger UI at: `http://localhost:8080/swagger-ui.html`
- Swagger JSON: `http://localhost:8080/v2/api-docs`
- SOAP Endpoint: `http://localhost:8080/services/customers.wsdl`
- Sonar on local: `mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=c0bee1da6fc598716e80e82011ff6c25ebb75395`