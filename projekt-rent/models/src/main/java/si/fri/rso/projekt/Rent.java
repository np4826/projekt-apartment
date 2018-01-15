package si.fri.rso.projekt;

import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "rents")
@NamedQueries(value =
        {
                @NamedQuery(name = "Rent.getAll", query = "SELECT r FROM rents r"),
                @NamedQuery(name = "Rent.getByApartmentAndDates", query = "SELECT r FROM rents r " +
                        "WHERE r.apartmentId = :apartmentId " +
                        "AND r.rentStart >= :rStart " +
                        "AND r.rentEnd <= :rEnd")
        })
@UuidGenerator(name = "idGenerator")
public class Rent {
    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    //DETAILS
    @Column(name = "rent_start")
    private Date rentStart;
    @Column(name = "rent_end")
    private Date rentEnd;
    private double price;
    private String comment;

    //APPROVED
    private Date dateApproved;
    private boolean approved;

    //CANCELED
    private boolean canceled;
    private String cancelationMessage;

    //CLIENT
    @Column(name = "user_id")
    private String userId;

    //APARTMENT
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

    public Date getRentStart() {
        return rentStart;
    }

    public void setRentStart(Date start) {
        this.rentStart = start;
    }

    public Date getRentEnd() {
        return rentEnd;
    }

    public void setEnd(Date end) {
        this.rentEnd = end;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDateApproved() {
        return dateApproved;
    }

    public void setDateApproved(Date dateApproved) {
        this.dateApproved = dateApproved;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public String getCancelationMessage() {
        return cancelationMessage;
    }

    public void setCancelationMessage(String cancelationMessage) {
        this.cancelationMessage = cancelationMessage;
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
}
