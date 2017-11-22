package si.fri.rso.projekt.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.fault.tolerance.annotations.*;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import si.fri.rso.projekt.Apartment;
import si.fri.rso.projekt.User;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RequestScoped
@GroupKey("apartment")
public class UserBean {

    private Logger log = LogManager.getLogger(UserBean.class.getName());

    @Inject
    private EntityManager em;

    private ObjectMapper objectMapper;

    private HttpClient httpClient;

    @Inject
    private UserBean userBean;

    @Inject
    @DiscoverService(value = "rso-apartment")
    private Optional<String> basePath;

    @PostConstruct
    private void init() {
        httpClient = HttpClientBuilder.create().build();
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


    public List<Apartment> getApartments(String userId) {

        if (basePath.isPresent()) {
            try {
                HttpGet request = new HttpGet(basePath.get() + "/v1/apartment?where=userId:EQ:" + userId);
                HttpResponse response = httpClient.execute(request);
                //http://localhost:8081/v1/apartment?where=userId:EQ:1
                int status = response.getStatusLine().getStatusCode();
                log.info("Basepath:" + basePath.get());
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();

                    if (entity != null)
                        return getObjects(EntityUtils.toString(entity));
                } else {
                    String msg = "Remote server '" + basePath.get() + "' is responded with status " + status + ".";
                    log.error(msg);
                    throw new InternalServerErrorException(msg);
                }

            } catch (IOException e) {
                String msg = e.getClass().getName() + " occured: " + e.getMessage();
                log.error(msg);
                throw new InternalServerErrorException(msg);
            }
        } else {
            throw new InternalServerErrorException("Apartments service not available");
        }
        return new ArrayList<>();
    }

    public List<Apartment> getApartmentsFallback(String userId) {
        return new ArrayList<>();
    }

    private List<Apartment> getObjects(String json) throws IOException {
        return json == null ? new ArrayList<>() : objectMapper.readValue(json,
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).getTypeFactory().constructCollectionType(List.class, Apartment.class));
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
