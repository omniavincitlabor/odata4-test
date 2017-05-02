package test.services;


import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.annotations.EdmFunction;
import com.sdl.odata.api.edm.annotations.EdmReturnType;
import com.sdl.odata.api.processor.datasource.factory.DataSourceFactory;
import com.sdl.odata.api.service.ODataRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.AbstractBoundOperation;
import test.domain.Person;


import java.util.List;

/**
 *
 */
@EdmFunction(name = "GetAverageAge", namespace = "SDL.OData.Example", isBound = true)
@EdmReturnType(type = "Edm.Double")
public class GetAverageAge extends AbstractBoundOperation<List<Person>, Double> {
    private static final Logger LOG = LoggerFactory.getLogger(GetAverageAge.class);

    @Override
    public String getBaseType() {
        return "Persons";
    }

    @Override
    public boolean baseTypeIsCollection() {
        return true;
    }

    @Override
    public Double doOperation(final ODataRequestContext requestContext,
                              final DataSourceFactory dataSourceFactory,
                              final List<Person> personList) throws ODataException {
        LOG.debug("Executing function 'GetAverageAge'");

        final Double result = personList.stream().mapToInt(Person::getAge).average().getAsDouble();

        LOG.debug("Average age: {}", result);

        return result;
    }
}