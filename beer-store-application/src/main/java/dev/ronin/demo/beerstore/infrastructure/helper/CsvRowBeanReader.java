package dev.ronin.demo.beerstore.infrastructure.helper;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvException;
import org.supercsv.exception.SuperCsvReflectionException;
import org.supercsv.util.BeanInterfaceProxy;
import org.supercsv.util.CsvContext;
import org.supercsv.util.MethodCache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CsvRowBeanReader<T> {

    private final Class<T> clazz;
    private final String[] nameMapping;
    private final CellProcessor[] processors;

    public CsvRowBeanReader(final Class<T> clazz, final String[] nameMapping, final CellProcessor... processors) {
        if (clazz == null) {
            throw new NullPointerException("clazz should not be null");
        } else if (nameMapping == null) {
            throw new NullPointerException("nameMapping should not be null");
        } else if (processors == null) {
            throw new NullPointerException("processors should not be null");
        }

        this.clazz = clazz;
        this.nameMapping = nameMapping;
        this.processors = processors;
    }

    public T read(final List<String> row, final CsvContext context) {
        return populateBean(instantiateBean(clazz), nameMapping, processRow(row, processors, context));
    }

    private static List<Object> processRow(final List<?> source, CellProcessor[] processors, final CsvContext context) {
        if (source == null) {
            throw new NullPointerException("source should not be null");
        } else if (processors == null) {
            throw new NullPointerException("processors should not be null");
        }

        context.setRowSource(new ArrayList<Object>(source));

        if (source.size() != processors.length) {
            throw new SuperCsvException(String.format(
                    "The number of columns to be processed (%d) must match the number of CellProcessors (%d): check that the number"
                            + " of CellProcessors you have defined matches the expected number of columns being read/written",
                    source.size(), processors.length), context);
        }

        List<Object> destination = new ArrayList<>();
        for (int i = 0; i < source.size(); i++) {

            context.setColumnNumber(i + 1); // update context (columns start at 1)

            if (processors[i] == null) {
                destination.add(source.get(i)); // no processing required
            } else {
                destination.add(processors[i].execute(source.get(i), context)); // execute the processor chain
            }
        }

        return destination;
    }

    private static <T> T instantiateBean(final Class<T> clazz) {
        final T bean;
        if (clazz.isInterface()) {
            bean = BeanInterfaceProxy.createProxy(clazz);
        } else {
            try {
                bean = clazz.newInstance();
            } catch (InstantiationException e) {
                throw new SuperCsvReflectionException(String.format(
                        "error instantiating bean, check that %s has a default no-args constructor", clazz.getName()), e);
            } catch (IllegalAccessException e) {
                throw new SuperCsvReflectionException("error instantiating bean", e);
            }
        }

        return bean;
    }

    private T populateBean(final T resultBean, final String[] nameMapping, final List<Object> processedColumns) {
        final MethodCache cache = new MethodCache();
        // map each column to its associated field on the bean
        for (int i = 0; i < nameMapping.length; i++) {

            final Object fieldValue = processedColumns.get(i);

            // don't call a set-method in the bean if there is no name mapping for the column or no result to store
            if (nameMapping[i] == null || fieldValue == null) {
                continue;
            }

            // invoke the setter on the bean
            Method setMethod = cache.getSetMethod(resultBean, nameMapping[i], fieldValue.getClass());
            invokeSetter(resultBean, setMethod, fieldValue);

        }

        return resultBean;
    }

    private static void invokeSetter(final Object bean, final Method setMethod, final Object fieldValue) {
        try {
            setMethod.invoke(bean, fieldValue);
        } catch (final Exception e) {
            throw new SuperCsvReflectionException(String.format("error invoking method %s()", setMethod.getName()), e);
        }
    }

}