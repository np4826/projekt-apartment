package si.fri.rso.projekt;

import org.eclipse.persistence.annotations.UuidGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "avaliability")
@NamedQueries(value =
        {
                @NamedQuery(name = "Avaliability.getAll", query = "SELECT r FROM avaliability r"),
        })
@UuidGenerator(name = "idGenerator")
public class Avaliability {
    @Id
    @GeneratedValue(generator = "idGenerator")
    private String id;

    //DETAILS
    @Column(name = "avaliability_start")
    private Date avaliabilityStart;
    @Column(name = "avaliability_end")
    private Date avaliabilityEnd;
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

    public Date getAvaliabilityStart() {
        return avaliabilityStart;
    }

    public void setAvaliabilityStart(Date avaliabilityStart) {
        this.avaliabilityStart = avaliabilityStart;
    }

    public Date getAvaliabilityEnd() {
        return avaliabilityEnd;
    }

    public void setAvaliabilityEnd(Date avaliabilityEnd) {
        this.avaliabilityEnd = avaliabilityEnd;
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
