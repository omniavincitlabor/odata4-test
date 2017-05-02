package test.services;


import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.edm.annotations.EdmFunction;
import com.sdl.odata.api.edm.annotations.EdmParameter;
import com.sdl.odata.api.edm.annotations.EdmReturnType;
import com.sdl.odata.api.processor.datasource.factory.DataSourceFactory;
import com.sdl.odata.api.service.ODataRequestContext;
import test.AbstractBoundOperation;
import test.domain.Person;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@EdmFunction(name = "GetAllAboveAge", namespace = "SDL.OData.Example", isBound = true)
@EdmReturnType(type = "Persons")
public class GetAllAboveAge extends AbstractBoundOperation<List<Person>, List<Person>> {

    @EdmParameter(name = "age", type = "Edm.Double")
    private Double age;

    public Double getAge() {
        return age;
    }

    public void setAge(final Double age) {
        this.age = age;
    }

    @Override
    public String getBaseType() {
        return "Persons";
    }


    @Override
    public boolean baseTypeIsCollection() {
        return true;
    }

    @Override
    public List<Person> doOperation(final ODataRequestContext requestContext,
                                    final DataSourceFactory dataSourceFactory,
                                    final List<Person> personList) throws ODataException {

        return personList.stream().filter(person -> person.getAge() > getAge()).collect(Collectors.toList());
    }
}