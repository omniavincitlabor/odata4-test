package test;


import com.sdl.odata.client.BasicODataClientQuery;
import com.sdl.odata.client.BoundFunctionClientQuery;
import com.sdl.odata.client.ClientPropertiesBuilder;
import com.sdl.odata.client.DefaultODataClient;
import com.sdl.odata.client.ODataV4ClientComponentsProvider;
import com.sdl.odata.client.api.ODataClientQuery;
import test.domain.BlubbPerson;
import test.domain.Company;
import test.domain.Person;
import test.domain.School;

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
        classes.add(School.class.getName());


        final ClientPropertiesBuilder cpb = new ClientPropertiesBuilder()
                .withServiceUri("http://localhost:8081/example.svc");
        final ODataV4ClientComponentsProvider componentsProvider = new ODataV4ClientComponentsProvider(classes, cpb.build());
        // Create and configure the client
        final DefaultODataClient client = new DefaultODataClient();
        //important for function call
        client.encodeURL(false);
        client.configure(componentsProvider);

        //Basic Query with Expand
        ODataClientQuery query = new BasicODataClientQuery.Builder()
                .withEntityType(Person.class)
                .withExpandParameters("company")
                .withExpandParameters("school")
                .build();

        List<Object> entities = (List<Object>) client.getEntities(Collections.emptyMap(), query);
        System.out.println(entities);

        //Single-Object-Query
        query = new FilterableODataClientQuery.Builder()
                .withEntityType(Person.class)
                .withExpandParameters("company")
                .withExpandParameters("school")
                .withEntityKey("MyHero", true)
                .build();

        entities = (List<Object>) client.getEntities(Collections.emptyMap(), query);
        System.out.println(entities);

        //Function Query with Parameter
        query = new BoundFunctionClientQuery.Builder()
                .withBoundEntityName("Persons")
                .withEntityType(Person.class)
                .withNameSpace("SDL.OData.Example")
                .withFunctionName("GetAllAboveAge")
                .withFunctionParameter("age", "20")
                .build();

        entities = (List<Object>) client.getEntities(Collections.emptyMap(), query);
        System.out.println(entities);

        //Complex Query
        query = new FilterableODataClientQuery.Builder()
                .withEntityType(Person.class)
                .withExpandParameters("company")
                .withExpandParameters("school")
                .withFilter(new FilterableODataClientQuery.Filter("age",
                                                                  IS_GREATER_THAN,
                                                                  "20",
                                                                  false))
                .build();

        entities = (List<Object>) client.getEntities(Collections.emptyMap(), query);
        System.out.println(entities);

        //Join-Query
        query = new JoinODataClientQuery.Builder()
                .withEntityType(Person.class)
                .withJoinPropertyName("persons")
                .withLeftQuery(new BasicODataClientQuery.Builder()
                                       .withEntityType(Company.class)
                                       .withEntityKey("1")
                                       .build())
                .withRightQuery(new BasicODataClientQuery.Builder()
                                        .withEntityType(Person.class)
                                        .withFilterMap("firstName", "Darkwing")
                                        .build())
                .build();

        entities = (List<Object>) client.getEntities(Collections.emptyMap(), query);
        System.out.println(entities);

        //Nested Join-Query
        query = new JoinODataClientQuery.Builder()
                .withEntityType(Person.class)
                .withJoinPropertyName("school")
                .withLeftQuery(new JoinODataClientQuery.Builder()
                                       .withEntityType(Person.class)
                                       .withJoinPropertyName("persons")
                                       .withLeftQuery(new BasicODataClientQuery.Builder()
                                                              .withEntityType(Company.class)
                                                              .withEntityKey("1")
                                                              .build())
                                       .withRightQuery(new FilterableODataClientQuery.Builder()
                                                               .withEntityType(Person.class)
                                                               .withEntityKey("MyHero", true)
                                                               .build())
                                       .build())
                .withRightQuery(new BasicODataClientQuery.Builder()
                                        .withEntityType(School.class)
                                        .build())
                .build();

        entities = (List<Object>) client.getEntities(Collections.emptyMap(), query);
        System.out.println(entities);
    }
}
