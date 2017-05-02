package test;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.sdl.odata.api.ODataException;
import com.sdl.odata.api.ODataSystemException;
import com.sdl.odata.api.edm.model.EntityDataModel;
import com.sdl.odata.api.parser.ODataUri;
import com.sdl.odata.api.parser.ODataUriUtil;
import com.sdl.odata.api.processor.datasource.ODataDataSourceException;
import com.sdl.odata.api.processor.datasource.TransactionalDataSource;
import com.sdl.odata.api.processor.link.ODataLink;
import scala.Option;

import test.domain.BlubbPerson;
import test.domain.Company;
import test.domain.Person;
import test.domain.School;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class TestDataSource implements IndexedCollectionDataSource {

    private static List<Class<?>> SUITABLE_CLASSES = Arrays.asList(Person.class, BlubbPerson.class, Company.class, School.class);

    private ConcurrentMap<String, Person> personConcurrentMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Long, Company> companyConcurrentMap = new ConcurrentHashMap<>();
    private ConcurrentMap<Long, School> schoolConcurrentMap = new ConcurrentHashMap<>();


    @Override
    public Object create(final ODataUri oDataUri,
                         final Object o,
                         final EntityDataModel entityDataModel) throws ODataException {
        if (o instanceof Person) {
            final Person person = (Person) o;
            if (personConcurrentMap.putIfAbsent(person.getPersonId(), person) != null) {
                throw new ODataDataSourceException("Could not create entity, already exists");
            }

            return person;
        }
        if (o instanceof Company) {
            final Company company = (Company) o;
            if (companyConcurrentMap.putIfAbsent(company.getId(), company) != null) {
                throw new ODataDataSourceException("Could not create entity, already exists");
            }

            return company;
        }
        if (o instanceof School) {
            final School company = (School) o;
            if (schoolConcurrentMap.putIfAbsent(company.getId(), company) != null) {
                throw new ODataDataSourceException("Could not create entity, already exists");
            }

            return company;
        }
        return null;
    }

    @Override
    public Object update(final ODataUri oDataUri,
                         final Object o,
                         final EntityDataModel entityDataModel) throws ODataException {
        if (o instanceof Person) {
            final Person person = (Person) o;
            if(personConcurrentMap.containsKey(person.getPersonId())) {
                personConcurrentMap.put(person.getPersonId(), person);

                return person;
            } else {
                throw new ODataDataSourceException("Unable to update person, entity does not exist");
            }
        }
        if (o instanceof Company) {
            final Company company = (Company) o;
            if(companyConcurrentMap.containsKey(company.getId())) {
                companyConcurrentMap.put(company.getId(), company);

                return company;
            } else {
                throw new ODataDataSourceException("Unable to update person, entity does not exist");
            }
        }
        if (o instanceof School) {
            final School company = (School) o;
            if(schoolConcurrentMap.containsKey(company.getId())) {
                schoolConcurrentMap.put(company.getId(), company);

                return company;
            } else {
                throw new ODataDataSourceException("Unable to update person, entity does not exist");
            }
        }
        return null;
    }

    @Override
    public void delete(final ODataUri oDataUri,
                       final EntityDataModel entityDataModel) throws ODataException {
        final Option<Object> entity = ODataUriUtil.extractEntityWithKeys(oDataUri, entityDataModel);
        if(entity.isDefined()) {
            if (entity.get() instanceof Person) {
                Person person = (Person) entity.get();
                personConcurrentMap.remove(person.getPersonId());
            }
            if (entity.get() instanceof Company) {
                Company company = (Company) entity.get();
                companyConcurrentMap.remove(company.getId());
            }
            if (entity.get() instanceof School) {
                School company = (School) entity.get();
                schoolConcurrentMap.remove(company.getId());
            }
        }
    }

    @Override
    public TransactionalDataSource startTransaction() {
        throw new ODataSystemException("No support for transactions");
    }


    @Override
    public List<Class<?>> getSuitableClasses() {
        return SUITABLE_CLASSES;
    }

    public ConcurrentMap<String, Person> getPersonConcurrentMap() {
        return personConcurrentMap;
    }

    public TableProvider getTableProvider() {
        return new TableProvider() {
            @Override
            public <T> ConcurrentIndexedCollection<T> getIndexedCollectionForType(final Class<T> type) {
                if (type.equals(Person.class)) {
                    ConcurrentIndexedCollection<Person> list = new ConcurrentIndexedCollection<>();
                    list.addAll(personConcurrentMap.values());
                    return (ConcurrentIndexedCollection<T>) list;
                }
                if (type.equals(Company.class)) {
                    ConcurrentIndexedCollection<Company> list = new ConcurrentIndexedCollection<>();
                    list.addAll(companyConcurrentMap.values());
                    return (ConcurrentIndexedCollection<T>) list;
                }
                if (type.equals(School.class)) {
                    ConcurrentIndexedCollection<School> list = new ConcurrentIndexedCollection<>();
                    list.addAll(schoolConcurrentMap.values());
                    return (ConcurrentIndexedCollection<T>) list;
                }
                return new ConcurrentIndexedCollection<>();
            }
        };
    }

    @Override
    public void createLink(ODataUri oDataUri, ODataLink oDataLink, EntityDataModel entityDataModel) throws ODataException {

    }

    @Override
    public void deleteLink(ODataUri oDataUri, ODataLink oDataLink, EntityDataModel entityDataModel) throws ODataException {

    }
}