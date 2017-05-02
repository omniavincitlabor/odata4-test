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
public class School implements Comparable<School>{

    @EdmProperty(name = "id", nullable = false)
    private long id;

    @EdmProperty(name = "name", nullable = false)
    private String name;

    @EdmNavigationProperty(name = "persons", nullable = true, partner = "school")
    private List<Person> persons;

    @EdmProperty(name = "type", nullable = true)
    private SchoolType type;

    public School(final long id, final String name, final SchoolType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public School() {

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

    public SchoolType getType() {
        return type;
    }

    public void setType(final SchoolType type) {
        this.type = type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final School school = (School) o;
        return id == school.id &&
               Objects.equals(name, school.name) &&
               Objects.equals(persons, school.persons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, persons);
    }

    @Override
    public int compareTo(final School o) {
        if (name.compareTo(o.getName()) == 0) {
            return Long.compare(id, o.getId());
        }
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return "School{"
               + "id=" + id
               + ", name='" + name + '\''
               + '}';
    }
}
