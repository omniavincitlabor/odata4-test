package test.domain;

import com.sdl.odata.api.edm.annotations.EdmEntity;
import com.sdl.odata.api.edm.annotations.EdmEntitySet;
import com.sdl.odata.api.edm.annotations.EdmProperty;

/**
 *
 */
@EdmEntity(namespace = "SDL.OData.Example", key = "blubb", containerName = "SDLExample")
@EdmEntitySet
public class BlubbPerson extends Person {

    @EdmProperty(name = "blubb", nullable = false)
    private int blubb;


    public BlubbPerson(final String personId,
                       final String firstName,
                       final String lastName,
                       final int age,
                       final Company company,
                       final School school,
                       final int blubb) {
        super(personId, firstName, lastName, age, blubb, company, school);
        this.blubb = blubb;
    }

    public BlubbPerson() {

    }

    public int getBlubb() {
        return blubb;
    }

    @Override
    public String toString() {
        return "BlubbPerson{"
               + "personId='" + getPersonId() + '\''
               + ", firstName='" + getFirstName() + '\''
               + ", lastName='" + getLastName() + '\''
               + ", age=" + getAge()
               + ", foo=" + getFoo()
               + ", company=" + getCompany()
               + ", school=" + getSchool()
               + ", blubb=" + blubb
               + '}';
    }
}
