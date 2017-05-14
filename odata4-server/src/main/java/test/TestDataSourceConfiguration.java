package test;


import com.sdl.odata.api.edm.registry.ODataEdmRegistry;
import com.sdl.odata.api.processor.datasource.DataSourceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import test.domain.*;
import test.services.GetAllAboveAge;
import test.services.GetAverageAge;
import test.services.GetAveragePersonAge;
import test.services.GetBlubb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
@Configuration
public class TestDataSourceConfiguration {

    @Bean
    IndexedCollectionDataSource testDataSource() {
        final TestDataSource testDataSource = new TestDataSource();

        final List<Company> companyList = Arrays.asList(
                new Company(1, "foo"),
                new Company(2, "bar")
        );

        final List<School> schoolList = Arrays.asList(
                new School(1, "foo1234", SchoolType.PRIMARY),
                new School(2, "bar1234", SchoolType.SECONDARY)
        );

        final List<Department> departments = Arrays.asList(
                new Department(1, "Finance", companyList.get(0)),
                new Department(2, "Production", companyList.get(0)),
                new Department(3, "Finance", companyList.get(1))
        );

        final List<Person> personList = Arrays.asList(
                new Person("MyHero", "Darkwing", "Duck", 23, 4, departments.get(0), schoolList.get(1)),
                new BlubbPerson("Sidekick", "Launchpad", "McQuack", 35, departments.get(1), schoolList.get(0), 3),
                new Person("Waddlemeyer", "Gosalyn", "Mallard", 9, 25, departments.get(2), schoolList.get(1)));

        try {
            for (Person person : personList) {
                testDataSource.create(null, person, null);
            }

            for (Company company : companyList) {
                testDataSource.create(null, company, null);
            }

            for (School school : schoolList) {
                testDataSource.create(null, school, null);
            }

            for (Department department : departments) {
                testDataSource.create(null, department, null);
            }
        } catch(Throwable t) {
            //ignore
        }
        return testDataSource;
    }

    @Bean
    DataSourceProvider inMemoryDataSourceProvider(final IndexedCollectionDataSource testDataSource) {
        return new GenericInMemoryDataSourceProvider(testDataSource);
    }

    @Bean
    EntityServiceRegistar entityServiceRegistar(final ODataEdmRegistry oDataEdmRegistry) {
        return new EntityServiceRegistar(oDataEdmRegistry) {
            @Override
            public void registerEntities(final ODataEdmRegistry oDataEdmRegistry) {
                oDataEdmRegistry.registerClasses(Arrays.asList(
                        Person.class,
                        Department.class,
                        Company.class,
                        BlubbPerson.class,
                        School.class,
                        GetAverageAge.class,
                        GetAveragePersonAge.class,
                        GetAllAboveAge.class,
                        GetBlubb.class,
                        SchoolType.class
                ));
            }
        };
    }
}
