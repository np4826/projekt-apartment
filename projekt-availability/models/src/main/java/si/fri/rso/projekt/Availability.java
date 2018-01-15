package si.fri.rso.projekt;

import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "availability")
@NamedQueries(value =
        {
                @NamedQuery(name = "Availability.getAll", query = "SELECT r FROM availability r"),
                @NamedQuery(name = "Availability.getByApartmentAndDates", query = "SELECT r FROM availability r " +
                        "WHERE r.apartmentId = :apartmentId " +
                        "AND r.availabilityStart <= :aStart " +
                        "AND r.availabilityEnd >= :aEnd")
        })
@UuidGenerator(name = "idGenerator")
public class Availability {
    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    //DETAILS
    @Column(name = "availability_start")
    private Date availabilityStart;
    @Column(name = "availability_end")
    private Date availabilityEnd;
    private String comment;

    //APARTMENT
    @Column(name = "apartment_id")
    private String apartmentId;

    @Transient
    private Apartment apartment;

    //GETTERS SETTERS
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getAvailabilityStart() {
        return availabilityStart;
    }

    public void setAvailabilityStart(Date availabilityStart) {
        this.availabilityStart = availabilityStart;
    }

    public Date getAvailabilityEnd() {
        return availabilityEnd;
    }

    public void setAvailabilityEnd(Date availabilityEnd) {
        this.availabilityEnd = availabilityEnd;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
}
