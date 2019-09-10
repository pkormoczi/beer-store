package dev.ronin.demo.beerstore.domain.customer;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class CustomerCsvReaderTest {
    @Test
    void testImport() throws IOException {
        CustomerCsvReader importer = new CustomerCsvReader();
        importer.read(new File(getClass().getClassLoader().getResource("persons.csv").getFile()));

    }
}