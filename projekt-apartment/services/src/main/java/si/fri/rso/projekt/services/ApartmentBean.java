package si.fri.rso.projekt.services;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import si.fri.rso.projekt.Apartment;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class ApartmentBean {
    @Inject
    private EntityManager em;
    private Logger log = Logger.getLogger(ApartmentBean.class.getName());

    public List<Apartment> getApartments(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, Apartment.class, queryParameters);
    }

    public Apartment getApartment(String apartmentId) {

        Apartment apartment = em.find(Apartment.class, apartmentId);

        if (apartment == null) {
            throw new NotFoundException();
        }

        return apartment;
    }

    public Apartment createApartment(Apartment apartment) {

        try {
            beginTx();
            em.persist(apartment);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return apartment;
    }

    public Apartment putApartment(String apartmentId, Apartment apartment) {

        Apartment c = em.find(Apartment.class, apartmentId);

        if (c == null) {
            return null;
        }

        try {
            beginTx();
            apartment.setId(c.getId());
            apartment = em.merge(apartment);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return apartment;
    }

    public boolean deleteApartment(String apartmentId) {

        Apartment apartment = em.find(Apartment.class, apartmentId);

        if (apartment != null) {
            try {
                beginTx();
                em.remove(apartment);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else
            return false;

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }
}
