package dev.ronin.demo.beerstore.infrastructure.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import dev.ronin.demo.beerstore.contract.customerdata.CustomerModel;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class Query implements GraphQLQueryResolver {

    public List<CustomerModel> getCustomers(int count, int offset){
        return Collections.singletonList(new CustomerModel());
    }
}