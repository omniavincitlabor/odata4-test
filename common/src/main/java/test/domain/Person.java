package test.domain;


import com.sdl.odata.api.edm.annotations.EdmEntity;
import com.sdl.odata.api.edm.annotations.EdmEntitySet;
import com.sdl.odata.api.edm.annotations.EdmNavigationProperty;
import com.sdl.odata.api.edm.annotations.EdmProperty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

@EdmEntity(namespace = "SDL.OData.Example", key = "id", containerName = "SDLExample")
@EdmEntitySet
public class Person implements Comparable<Person> {

    @EdmProperty(name = "id", nullable = false)
    private String personId;

    @EdmProperty(name = "firstName", nullable = false)
    private String firstName;

    @EdmProperty(name = "lastName", nullable = false)
    private String lastName;

    @EdmProperty(name = "age", nullable = false)
    private int age;

    @EdmProperty(name = "foo", nullable = false)
    private int foo;

    @EdmNavigationProperty(name = "company", nullable = true)
    private Company company;

    @EdmNavigationProperty(name = "school", nullable = true)
    private School school;

    public Person(final String personId,
                  final String firstName,
                  final String lastName,
                  final int age,
                  final int foo,
                  final Company company,
                  final School school) {
        this.personId = personId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.foo = foo;

        this.company = company;
        this.school = school;

        if (company.getPersons() == null) {
            company.setPersons(new ArrayList<>());
        }
        company.getPersons().add(this);

        if (school.getPersons() == null) {
            school.setPersons(new ArrayList<>());
        }
        school.getPersons().add(this);
    }

    public Person() {
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(final String personId) {
        this.personId = personId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public int getFoo() {
        return foo;
    }

    public void setFoo(final int foo) {
        this.foo = foo;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(final Company company) {
        this.company = company;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(final School school) {
        this.school = school;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Person person = (Person) o;
        return age == person.age &&
               Objects.equals(personId, person.personId) &&
               Objects.equals(firstName, person.firstName) &&
               Objects.equals(lastName, person.lastName) &&
               Objects.equals(company, person.company);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, firstName, lastName, age, company);
    }

    @Override
    public int compareTo(final Person o) {
        Comparator<Person> comparator = Comparator
                .comparing(Person::getFirstName)
                .thenComparing(Person::getLastName)
                .thenComparingInt(Person::getAge)
                .thenComparing(Person::getCompany)
                .thenComparing(Person::getSchool);
        return comparator.compare(this, o);
    }

    @Override
    public String toString() {
        return "Person{"
               + "personId='" + personId + '\''
               + ", firstName='" + firstName + '\''
               + ", lastName='" + lastName + '\''
               + ", age=" + age
               + ", foo=" + foo
               + ", company=" + company
               + ", school=" + school
               + '}';
    }
}