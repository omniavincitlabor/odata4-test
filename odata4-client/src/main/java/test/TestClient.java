package test;


import com.sdl.odata.client.ClientPropertiesBuilder;
import com.sdl.odata.client.DefaultODataClient;
import com.sdl.odata.client.ODataV4ClientComponentsProvider;
import com.sdl.odata.client.api.ODataClientQuery;
import test.domain.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static test.FilterableODataClientQuery.FilterOperation.IS_GREATER_THAN;

/**
 *
 */
public class TestClient {

    public static void main(String[] args) {
        final List<String> classes = new ArrayList<>();
        classes.add(Person.class.getName());
        classes.add(BlubbPerson.class.getName());
        classes.add(Company.class.getName());
        classes.add(Department.class.getName());
        classes.add(School.class.getName());


        final ClientPropertiesBuilder cpb = new ClientPropertiesBuilder()
                .withServiceUri("http://localhost:8080/example.svc");
        final ODataV4ClientComponentsProvider componentsProvider = new ODataV4ClientComponentsProvider(classes, cpb.build());
        // Create and configure the client
        final DefaultODataClient client = new DefaultODataClient();
        //important for function call
        client.encodeURL(false);
        client.configure(componentsProvider);

        //Basic Query with Expand
        ODataClientQuery query = new FilterableODataClientQuery.Builder()
                .withEntityType(Person.class)
                .withExpandParameters("department")
                .withExpandParameters("school")
                .build();

        List<Object> entities = (List<Object>) client.getEntities(Collections.emptyMap(), query);
        System.out.println(entities);

        //Single-Object-Query
        query = new FilterableODataClientQuery.Builder()
                .withEntityType(Person.class)
                .withExpandParameters("department")
                .withExpandParameters("school")
                .withEntityKey("id", "MyHero")
                .build();

        Object entity = client.getEntity(Collections.emptyMap(), query);
        System.out.println(entity);

        //Function Query with Parameter
        query = new MyBoundFunctionClientQuery.Builder()
                .withBoundEntityName("Persons")
                .withEntityType(Person.class)
                .withNameSpace("SDL.OData.Example")
                .withFunctionName("GetAllAboveAge")
                .withFunctionParameter("age", 20)
                .build();

        entities = (List<Object>) client.getEntities(Collections.emptyMap(), query);
        System.out.println(entities);

        //Complex Query
        query = new FilterableODataClientQuery.Builder()
                .withEntityType(Person.class)
                .withExpandParameters("department")
                .withExpandParameters("school")
                .withFilter(new FilterableODataClientQuery.Filter("age",
                                                                  IS_GREATER_THAN,
                                                                  20))
                .build();

        entities = (List<Object>) client.getEntities(Collections.emptyMap(), query);
        System.out.println(entities);

        //Join-Query
        query = new JoinODataClientQuery.Builder()
                .withEntityType(Department.class)
                .withJoinPropertyName("departments")
                .withLeftQuery(new FilterableODataClientQuery.Builder()
                                       .withEntityType(Company.class)
                                       .withEntityKey("id", 1)
                                       .build())
                .withRightQuery(new FilterableODataClientQuery.Builder()
                                        .withEntityType(Department.class)
                                        .withFilter(
                                                new FilterableODataClientQuery.Filter(
                                                        "name",
                                                        FilterableODataClientQuery.FilterFunction.STARTSWITH,
                                                        "Fin"))
                                        .build())
                .build();

        entities = (List<Object>) client.getEntities(Collections.emptyMap(), query);
        System.out.println(entities);

        //Nested Join-Query
        query = new JoinODataClientQuery.Builder()
                .withEntityType(School.class)
                .withJoinPropertyName("school")
                .withLeftQuery(new JoinODataClientQuery.Builder()
                                       .withEntityType(Person.class)
                                       .withJoinPropertyName("persons")
                                       .withLeftQuery(new FilterableODataClientQuery.Builder()
                                                              .withEntityType(Department.class)
                                                              .withEntityKey("id", 1)
                                                              .build())
                                       .withRightQuery(new FilterableODataClientQuery.Builder()
                                                               .withEntityType(Person.class)
                                                               .withEntityKey("id", "MyHero")
                                                               .build())
                                       .build())
                .withRightQuery(new FilterableODataClientQuery.Builder()
                                        .withEntityType(School.class)
                                        .build())
                .build();

        entity = client.getEntity(Collections.emptyMap(), query);
        System.out.println(entity);
    }
}
