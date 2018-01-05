package si.fri.rso.projekt.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import si.fri.rso.projekt.Apartment;
import si.fri.rso.projekt.User;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;

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
public class UserBean {

    private Logger log = LogManager.getLogger(UserBean.class.getName());

    @Inject
    private EntityManager em;

    private ObjectMapper objectMapper;

    private Client httpClient;

    @Inject
    private UserBean userBean;

    @Inject
    @DiscoverService(value = "rso-apartment")
    private Optional<String> basePath;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        objectMapper = new ObjectMapper();
    }

    public List<User> getUsers(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, User.class, queryParameters);
    }

    public User getUser(String userId) {

        User user = em.find(User.class, userId);

        if (user == null) {
            throw new NotFoundException();
        }

        return user;
    }

    public User getUserWithApartments(String userId) {

        User user = em.find(User.class, userId);

        if (user == null) {
            throw new NotFoundException();
        }
        List <Apartment> apartments = userBean.getApartments(userId);
        user.setApartments(apartments);
        return user;
    }

    public List<User> getUsersWithApartments(UriInfo uriInfo){
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();
        List<User> users = JPAUtils.queryEntities(em, User.class, queryParameters);
        for(User u : users){
            List <Apartment> apartments = userBean.getApartments(u.getId());
            u.setApartments(apartments);
        }
        return users;
    }


    public User createUser(User user) {

        try {
            beginTx();
            em.persist(user);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return user;
    }

    public User putUser(String userId, User user) {

        User c = em.find(User.class, userId);

        if (c == null) {
            return null;
        }

        try {
            beginTx();
            user.setId(c.getId());
            user = em.merge(user);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return user;
    }

    public boolean deleteUser(String userId) {

        User user = em.find(User.class, userId);

        if (user != null) {
            try {
                beginTx();
                em.remove(user);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else
            return false;

        return true;
    }

    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getApartmentsFallback")
    @Timeout
    public List<Apartment> getApartments(String userId) {
        log.info("IN GETAPARTMENTS");
        if (basePath.isPresent()) {
            log.info("IN GETAPARTMENTS BASEPATH");
            try {
                return httpClient
                        .target(basePath.get() + "/v1/apartment?where=userId:EQ:" + userId)
                        .request().get(new GenericType<List<Apartment>>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.error(e);
                throw new InternalServerErrorException(e);
            }
        }
        return new ArrayList<>();
    }

    public List<Apartment> getApartmentsFallback(String userId) {
        log.info("IN GETAPARTMENTS FALLBACK");
        List<Apartment> apartments = new ArrayList<>();
        Apartment apartment = new Apartment();
        apartment.setTitle("N/A");
        apartment.setDescription("N/A");
        apartments.add(apartment);
        return apartments;
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
