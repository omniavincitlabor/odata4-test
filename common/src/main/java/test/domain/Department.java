package test.domain;

import com.sdl.odata.api.edm.annotations.EdmEntity;
import com.sdl.odata.api.edm.annotations.EdmEntitySet;
import com.sdl.odata.api.edm.annotations.EdmNavigationProperty;
import com.sdl.odata.api.edm.annotations.EdmProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EdmEntity(namespace = "SDL.OData.Example", key = "id", containerName = "SDLExample")
@EdmEntitySet
public class Department implements Comparable<Department> {

    @EdmProperty(name = "id", nullable = false)
    private long id;

    @EdmProperty(name = "name", nullable = false)
    private String name;

    @EdmNavigationProperty(name = "company", nullable = true)
    private Company company;

    @EdmNavigationProperty(name = "persons", nullable = true, partner = "department")
    private List<Person> persons;

    public Department(final long id, final String name, final Company company) {
        this.id = id;
        this.name = name;
        this.company = company;

        if (company.getDepartments() == null) {
            company.setDepartments(new ArrayList<>());
        }
        company.getDepartments().add(this);
    }

    public Department() {

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
        final Department department = (Department) o;
        return id == department.id &&
                Objects.equals(name, department.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public int compareTo(final Department o) {
        if (name.compareTo(o.getName()) == 0) {
            return Long.compare(id, o.getId());
        }
        return name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return "Department{"
                + "id=" + id
                + ", name='" + name + '\''
                + '}';
    }
}
