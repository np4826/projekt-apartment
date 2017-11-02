package si.fri.rso.projekt;

import org.eclipse.persistence.annotations.UuidGenerator;
import javax.persistence.*;
import java.util.Date;
import si.fri.rso.projekt.Apartment;
import java.util.List;

@Entity(name = "users")
@NamedQueries(value =
{
    @NamedQuery(name = "User.getAll", query = "SELECT u FROM users u"),
  //  @NamedQuery(name = "User.findByCustomer", query = "SELECT u FROM userdata u WHERE u.userId = " +":userId")
})
@UuidGenerator(name = "idGenerator")
public class User {

    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    //GENERAL
    private String lastName;
    private Date birthday;

    //VISIBLE ON USER PROFILE
    private String firstName;
    private String description;
    // private String country;
    private Date joined;

    @Transient
    private List<Apartment> apartments;

    //METHODS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSurname() {
        return lastName;
    }

    public void setSurname(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String name) {
        this.firstName = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getJoined() {
        return joined;
    }

    public void setJoined(Date joined) {
        this.joined = joined;
    }

    public List<Apartment> getApartments() {
        return apartments;
    }

    public void setApartments(List<Apartment> apartments) {
        this.apartments = apartments;
    }
}
