package test.services;

import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.annotations.EdmFunction;
import com.sdl.odata.api.edm.annotations.EdmReturnType;
import com.sdl.odata.api.edm.model.Operation;
import com.sdl.odata.api.processor.datasource.factory.DataSourceFactory;
import com.sdl.odata.api.service.ODataRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.TestDataSource;
import test.domain.Person;

import java.util.concurrent.ConcurrentMap;

/**
 *
 */
@EdmFunction(name = "GetAveragePersonAge", namespace = "SDL.OData.Example", isBound = false)
@EdmReturnType(type = "Edm.Double")
public class GetAveragePersonAge implements Operation<Double> {
    private static final Logger LOG = LoggerFactory.getLogger(GetAveragePersonAge.class);

    @Override
    public Double doOperation(final ODataRequestContext requestContext,
                              final DataSourceFactory dataSourceFactory) throws ODataException {
        LOG.debug("Executing function 'GetAveragePersonAge'");

        TestDataSource dataSource = (TestDataSource) dataSourceFactory.getDataSource(requestContext, "SDL.OData.Example.Person");
        ConcurrentMap<String, Person> personConcurrentMap = dataSource.getPersonConcurrentMap();
        final Double result = personConcurrentMap.values().stream().mapToInt(Person::getAge).average().getAsDouble();

        LOG.debug("Average age: {}", result);

        return result;
    }
}