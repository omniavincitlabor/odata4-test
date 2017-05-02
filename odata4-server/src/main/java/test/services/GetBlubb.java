package test.services;


import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.annotations.EdmFunction;
import com.sdl.odata.api.edm.annotations.EdmReturnType;
import com.sdl.odata.api.processor.datasource.factory.DataSourceFactory;
import com.sdl.odata.api.service.ODataRequestContext;
import test.AbstractBoundOperation;
import test.domain.Person;


/**
 *
 */
@EdmFunction(name = "GetBlubb", namespace = "SDL.OData.Example", isBound = true)
@EdmReturnType(type = "Edm.Double")
public class GetBlubb extends AbstractBoundOperation<Person, Double> {

    @Override
    public String getBaseType() {
        return "SDL.OData.Example.Person";
    }


    @Override
    public boolean baseTypeIsCollection() {
        return false;
    }

    @Override
    public Double doOperation(final ODataRequestContext requestContext,
                              final DataSourceFactory dataSourceFactory,
                              final Person person) throws ODataException {
        final double foo = person.getFoo();
        final double age = person.getAge();
        return (foo + age) / foo;
    }
}