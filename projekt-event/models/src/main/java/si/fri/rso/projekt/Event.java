package si.fri.rso.projekt;

import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "events")
@NamedQueries(value =
        {
                @NamedQuery(name = "Event.getAll", query = "SELECT r FROM events r"),
                @NamedQuery(name = "Event.getRecommendedForUser", query = "SELECT e FROM events e WHERE e.userId != "+
                    ":userId ORDER BY e.eventPublished DESC"),
        })

@UuidGenerator(name = "idGenerator")
public class Event {
    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    //DETAILS
    @Column(name = "event_published", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Date eventPublished;
    private String message;

    //CONNECTED WITH
    @Column(name = "user_id")
    private String userId;

    @Column(name = "apartment_id")
    private String apartmentId;

    @Transient
    private Apartment apartment;

    @Transient
    private User user;

    //GETTERS SETTERS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(String apartmentId) {
        this.apartmentId = apartmentId;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getEventPublished() {
        return eventPublished;
    }

    public void setEventPublished(Date eventPublished) {
        this.eventPublished = eventPublished;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
