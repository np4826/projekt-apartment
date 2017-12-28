package si.fri.rso.projekt;

import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "rents")
@NamedQueries(value =
        {
                @NamedQuery(name = "Rent.getAll", query = "SELECT r FROM rents r"),
        })
@UuidGenerator(name = "idGenerator")
public class Rent {
    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    //DETAILS
    private Date start;
    private Date end;
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

    //GETTERS SETTERS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
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
}
