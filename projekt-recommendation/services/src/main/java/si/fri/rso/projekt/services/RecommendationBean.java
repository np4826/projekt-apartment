package si.fri.rso.projekt.services;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import si.fri.rso.projekt.Apartment;
import si.fri.rso.projekt.Recommendation;
import si.fri.rso.projekt.Rent;
import si.fri.rso.projekt.User;
import si.fri.rso.projekt.Event;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RequestScoped
public class RecommendationBean {

    private Logger log = LogManager.getLogger(RecommendationBean.class.getName());

    @Inject
    private EntityManager em;


    private Client httpClient;

    @Inject
    private RecommendationBean recommendationBean;

    @Inject
    @DiscoverService(value = "rso-apartment")
    private Optional<String> basePathApartment;

    @Inject
    @DiscoverService(value = "rso-user")
    private Optional<String> basePathUser;

    @Inject
    @DiscoverService(value = "rso-rent")
    private Optional<String> basePathRent;

    @Inject
    @DiscoverService(value = "rso-review")
    private Optional<String> basePathReview;

    @Inject
    @DiscoverService(value = "rso-event")
    private Optional<String> basePathEvent;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
    }

    public List<Recommendation> getRecommendations(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();


        List<Recommendation> recommendations = JPAUtils.queryEntities(em, Recommendation.class, queryParameters);
        for(Recommendation r : recommendations)
        {
            //r.setApartment(recommendationBean.getApartment(r.getApartmentId()));
            r.setUser(recommendationBean.getUser(r.getUserId()));
        }
        return recommendations;
    }


    public Recommendation getRecommendationSimple(String recommendationId) {

        Recommendation recommendation = em.find(Recommendation.class, recommendationId);

        if (recommendation == null) {
            throw new NotFoundException();
        }

        return recommendation;
    }

    public Recommendation getRecommendation(String recommendationId) {

        Recommendation recommendation = em.find(Recommendation.class, recommendationId);

        if (recommendation == null) {
            throw new NotFoundException();
        }

        //recommendation.setApartment(recommendationBean.getApartment(recommendation.getApartmentId()));
        recommendation.setUser(recommendationBean.getUser(recommendation.getUserId()));

        return recommendation;
    }

    public  Recommendation getRecommendationForUser(String userId) {
        //http://localhost:8085/v1/recommendation?filter=apartmentId:EQ:1
        QueryParameters queryParameters = QueryParameters.query("filter:userId:EQ:"+userId).defaultOffset(0)
                .build();

        List<Recommendation> recommendations = JPAUtils.queryEntities(em, Recommendation.class, queryParameters);

        if (recommendations.isEmpty()) {
            recommendations = createRecommendations(userId);
            if (recommendations == null)
                throw new NotFoundException();
        }

        return recommendations.get(0);
    }

    // get similar rents - userje order by dt
    // get reviews for apartments od userjev ali countkateri najpog.
    // priporoči vsaj 3. če ni ocene kdor najel?
    // preveri events za datum, če treba spreminjat? shrani?
    // če premalo dobi latest events
    // če še premalo reviewstoprated
    private List<Recommendation> createRecommendations(String userId){
        // get best reviews of users who had same rent
        // get all rents for user
        //http://localhost:8083/v1/rent?filter=userId:EQ:3
        List<Recommendation> recommendations = new ArrayList<>();
        Recommendation recommendation = new Recommendation();
        recommendation.setApartmentsId(new ArrayList<>());
        // Rent, userId, apartmentId

        List<Event> events = recommendationBean.getEvents(userId);
        if (events != null)
        for (Event e:  events) {
            if (e.getApartmentId() != null){
                recommendation.getApartmentsId().add(e.getApartmentId());
            }

        }
        recommendations.add(recommendation);
        return recommendations;
    }

    public Recommendation createRecommendation(Recommendation recommendation) {

        try {
            beginTx();
            em.persist(recommendation);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return recommendation;
    }

    public Recommendation putRecommendation(String recommendationId, Recommendation recommendation) {

        Recommendation c = em.find(Recommendation.class, recommendationId);

        if (c == null) {
            return null;
        }

        try {
            beginTx();
            recommendation.setId(c.getId());
            recommendation = em.merge(recommendation);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return recommendation;
    }

    public boolean deleteRecommendation(String recommendationId) {

        Recommendation recommendation = em.find(Recommendation.class, recommendationId);

        if (recommendation != null) {
            try {
                beginTx();
                em.remove(recommendation);
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
    public Apartment getApartment(String apartmentId) {
        log.info("IN GETAPARTMENT");
        if (basePathApartment.isPresent()) {
            log.info("GETTING appartment with ID "+apartmentId);
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
    @Fallback(fallbackMethod = "getUserFallback")
    @Timeout
    public User getUser(String userId) {
        log.info("IN GETUSER");
        if (basePathUser.isPresent()) {
            log.info("GETTING user with ID "+userId+" and url:"+basePathUser);
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

    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getRentsFallback")
    @Timeout
    public List<Rent> getRents(String userId) {
        //http://localhost:8083/v1/rent?filter=userId:EQ:3
        if (basePathRent.isPresent()) {
            try {
                return httpClient
                        .target(basePathRent.get() + "/v1/rent?filter=userId:EQ:" + userId)
                        .request().get(new GenericType<List<Rent>>()  {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.error(e);
                throw new InternalServerErrorException(e);
            }
        }
        return null;
    }

    public List<Rent> getRentsFallback(String apartmentId) {
        log.info("IN GETRENTS FALLBACK");
        Rent apartment = new Rent();
        apartment.setComment("N/A");
        return new ArrayList<Rent>();
    }

    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getEventsFallback")
    @Timeout
    public List<Event> getEvents(String userId) {
        //http://localhost:8086/v1/event/recommended/2
        if (basePathEvent.isPresent()) {
            try {
                return httpClient
                        .target(basePathEvent.get() + "/v1/event/recommended/" + userId)
                        .request().get(new GenericType<List<Event>>()  {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.error(e);
                throw new InternalServerErrorException(e);
            }
        }
        return null;
    }

    public List<Event> getEventsFallback(String apartmentId) {
        log.info("IN GETEvents FALLBACK");
        Event event = new Event();
        event.setMessage("N/A");
        return new ArrayList<Event>();
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
