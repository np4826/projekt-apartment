package si.fri.rso.projekt;

import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "payment")
@NamedQueries(value =
{
    @NamedQuery(name = "Payment.getAll", query = "SELECT a FROM payment a"),
    @NamedQuery(name = "Payment.findByRent", query = "SELECT a FROM payment a WHERE a.rentId = " +":rentId")
})
@UuidGenerator(name = "idGenerator")
public class Payment {

    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    @Column(name = "rent_id")
    private String rentId;

    private Date date;
    private double value;
    private String method;
    private String comment;
    private boolean finished;
    private boolean canceled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRentId() {
        return rentId;
    }

    public void setRentId(String rentId) {
        this.rentId = rentId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
