package test.domain;

import com.sdl.odata.api.edm.annotations.EdmEntity;
import com.sdl.odata.api.edm.annotations.EdmEntitySet;
import com.sdl.odata.api.edm.annotations.EdmNavigationProperty;
import com.sdl.odata.api.edm.annotations.EdmProperty;

import java.util.List;
import java.util.Objects;

/**
 *
 */
@EdmEntity(namespace = "SDL.OData.Example", key = "id", containerName = "SDLExample")
@EdmEntitySet
public class Company implements Comparable<Company> {

    @EdmProperty(name = "id", nullable = false)
    private long id;

    @EdmProperty(name = "name", nullable = false)
    private String name;

    @EdmNavigationProperty(name = "persons", nullable = true, partner = "company")
    private List<Person> persons;

    public Company(final long id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Company() {

    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(final List<Person> persons) {
        this.persons = persons;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Company company = (Company) o;
        return id == company.id &&
               Objects.equals(name, company.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public int compareTo(final Company o) {
        if (name.compareTo(o.getName()) == 0) {
            return Long.compare(id, o.getId());
        }
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return "Company{"
               + "id=" + id
               + ", name='" + name + '\''
               + '}';
    }
}
