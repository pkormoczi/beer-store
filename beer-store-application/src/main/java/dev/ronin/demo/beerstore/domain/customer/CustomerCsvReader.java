package dev.ronin.demo.beerstore.domain.customer;

import dev.ronin.demo.beerstore.infrastructure.helper.CsvRowBeanReader;
import lombok.extern.slf4j.Slf4j;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.UniqueHashCode;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CsvContext;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Slf4j
class CustomerCsvReader {

    void read(final File file) throws IOException {
        try (ICsvListReader listReader = new CsvListReader(new FileReader(file), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {

            final CellProcessor[] customerProcessor = getProcessors();

            CsvRowBeanReader<Customer> customerReader = new CsvRowBeanReader<>(Customer.class, listReader.getHeader(true), customerProcessor);

            List<String> row;
            while ((row = listReader.read()) != null) {
                if (row.size() == customerProcessor.length) {
                    final CsvContext context = new CsvContext(listReader.getLineNumber(), listReader.getRowNumber(), 1);
                    final Customer customer = customerReader.read(row, context);
                    log.info(String.format("lineNo=%s, rowNo=%s, customer=%s", listReader.getLineNumber(), listReader.getRowNumber(), customer));
                } else {
                    log.error("Hibás sor: Eltérő oszlop szám! - " + listReader.getUntokenizedRow());
                }
            }
        }
    }

    private CellProcessor[] getProcessors() {
        return new CellProcessor[]{
                new UniqueHashCode(new ParseLong()), // customerNo (must be unique)
                new NotNull(), // firstName
                new NotNull(), // l() {}astName
                new NotNull(new ParseAddress()), // address
        };

    }

    private class ParseAddress implements CellProcessor {


        @Override
        public Object execute(Object value, CsvContext context) {
            if (!(value instanceof String)) {
                throw new SuperCsvCellProcessorException(String.class, value, context, this);
            }
            final String[] address = ((String) value).split(",");
            return new Address(address[2], "", address[1], address[0]);
        }
    }
}
