package contracts

import org.springframework.cloud.contract.spec.Contract
import org.springframework.http.HttpStatus

Contract.make {
    description("When a GET request with a Customer name=Teszt is made, the Customer object is returned")
    request {
        method 'GET'
        url '/customers/TestFirst'
    }
    response {
        status HttpStatus.OK.value()
        body("""
          {
          "id": 1,
          "firstName": "TestFirst",
          "lastName": "TestLast",
          "address": {
            "country": "MockCountry",
            "city": "MockCity",
            "streetAddress": "MockAddress",
            "zip": "1111"
          }
        }
        """)
        headers {
            contentType(applicationJson())
        }
    }
}