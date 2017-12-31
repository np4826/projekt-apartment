package si.fri.rso.projekt;

import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "reviews")
@NamedQueries(value =
        {
                @NamedQuery(name = "Review.getAll", query = "SELECT r FROM reviews r"),
        })
@UuidGenerator(name = "idGenerator")
public class Review {
    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    //DETAILS
    @Column(name = "review_published")
    private Date reviewPublished;
    private String comment;
    private int rating;

    //PUBLISHER
    @Column(name = "user_id")
    private String userId;

    //REVIEW FOR APARTMENT
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public Date getReviewPublished() {
        return reviewPublished;
    }

    public void setReviewPublished(Date reviewPublished) {
        this.reviewPublished = reviewPublished;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
