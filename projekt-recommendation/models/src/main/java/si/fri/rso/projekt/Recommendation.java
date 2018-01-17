package si.fri.rso.projekt;

import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity(name = "recommendations")
@NamedQueries(value =
        {
                @NamedQuery(name = "Recommendation.getAll", query = "SELECT r FROM recommendations r"),
        })
@UuidGenerator(name = "idGenerator")
public class Recommendation {
    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="recommendation_saved", nullable = false,
            columnDefinition="TIMESTAMP default CURRENT_TIMESTAMP")
    private Date recommendationSaved;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "apartments_ids")
    private List<String> apartmentsId;

    @Transient
    private List<Apartment> apartments;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getRecommendationSaved() {
        return recommendationSaved;
    }

    public void setRecommendationSaved(Date recommendationSaved) {
        this.recommendationSaved = recommendationSaved;
    }

    public List<Apartment> getApartments() {
        return apartments;
    }

    public void setApartments(List<Apartment> apartments) {
        this.apartments = apartments;
    }


    public List<String> getApartmentsId() {
        return apartmentsId;
    }

    public void setApartmentsId(List<String> apartmentsId) {
        this.apartmentsId = apartmentsId;
    }
}
