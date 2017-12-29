package si.fri.rso.projekt.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import si.fri.rso.projekt.Apartment;
import si.fri.rso.projekt.Availability;
import si.fri.rso.projekt.User;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RequestScoped
public class AvailabilityBean {

    private Logger log = LogManager.getLogger(AvailabilityBean.class.getName());

    @Inject
    private EntityManager em;

    private ObjectMapper objectMapper;

    private Client httpClient;

    @Inject
    private AvailabilityBean availabilityBean;

    @Inject
    @DiscoverService(value = "rso-apartment")
    private Optional<String> basePathApartment;

    @Inject
    @DiscoverService(value = "rso-user")
    private Optional<String> basePathUser;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        objectMapper = new ObjectMapper();
    }

    public List<Availability> getAvaliabilities(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();

        List<Availability> avaliabilities = JPAUtils.queryEntities(em, Availability.class, queryParameters);
        for (Availability r : avaliabilities) {
            r.setApartment(getApartment(r.getApartmentId()));
        }
        return avaliabilities;
    }

    public Availability getAvailability(String rentId) {

        Availability availability = em.find(Availability.class, rentId);

        if (availability == null) {
            throw new NotFoundException();
        }

        return availability;
    }

    public Availability createAvailability(Availability availability) {

        try {
            beginTx();
            em.persist(availability);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return availability;
    }

    public Availability putAvailability(String rentId, Availability availability) {

        Availability c = em.find(Availability.class, rentId);

        if (c == null) {
            return null;
        }

        try {
            beginTx();
            availability.setId(c.getId());
            availability = em.merge(availability);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return availability;
    }

    public boolean deleteAvailability(String rentId) {

        Availability availability = em.find(Availability.class, rentId);

        if (availability != null) {
            try {
                beginTx();
                em.remove(availability);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else
            return false;

        return true;
    }

    private List<Apartment> getObjects(String json) throws IOException {
        return json == null ? new ArrayList<>() : objectMapper.readValue(json,
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).getTypeFactory().constructCollectionType(List.class, Apartment.class));
    }

    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getApartmentsFallback")
    @Timeout
    public Apartment getApartment(String apartmentId) {
        log.info("IN GETAPARTMENT");
        if (basePathApartment.isPresent()) {
            log.info("BASEPATH for APARTMENT is present");
            log.info("GETTING appartment with ID " + apartmentId);
            try {
                return httpClient
                        .target(basePathApartment.get() + "/v1/apartment/" + apartmentId)
                        .request().get(new GenericType<Apartment>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.info("ERROR IN GETAPARTMENT");
                log.error(e);
                throw new InternalServerErrorException(e);
            }
        }
        return null;
    }

    public Apartment getApartmentsFallback(String apartmentId) {
        log.info("IN GETAPARTMENT FALLBACK");
        Apartment apartment = new Apartment();
        apartment.setTitle("N/A");
        apartment.setDescription("N/A");
        return apartment;
    }

    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getApartmentsFallback")
    @Timeout
    public User getUser(String userId) {
        log.info("IN GETUSER");
        if (basePathUser.isPresent()) {
            log.info("BASEPATH for USER is present");
            log.info("GETTING user with ID " + userId);
            try {
                return httpClient
                        .target(basePathUser.get() + "/v1/user/simple/" + userId)
                        .request().get(new GenericType<User>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.info("ERROR IN GETUSER");
                log.error(e);
                throw new InternalServerErrorException(e);
            }
        }
        log.info("BASEPATH for USER is not present");
        return null;
    }

    public User getUserFallback(String userId) {
        log.info("IN GETUSER FALLBACK");
        User user = new User();
        user.setFirstName("N/A");
        return user;
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
