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
import si.fri.rso.projekt.Event;
import si.fri.rso.projekt.User;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.stream.Collectors;


@ApplicationScoped
public class EventBean {

    private Logger log = LogManager.getLogger(EventBean.class.getName());

    @Inject
    private EntityManager em;


    private Client httpClient;

    @Inject
    private EventBean eventBean;

    @Inject
    @DiscoverService(value = "rso-apartment")
    private Optional<String> basePathApartment;

    @Inject
    @DiscoverService(value = "rso-user")
    private Optional<String> basePathUser;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
    }

    public List<Event> getEvents(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();


        List<Event> events = JPAUtils.queryEntities(em, Event.class, queryParameters);
        for(Event r : events)
        {
            r.setApartment(eventBean.getApartment(r.getApartmentId()));
            r.setUser(eventBean.getUser(r.getUserId()));
        }
        return events;
    }

    public List<Event> getEventsFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        List<Event> events = JPAUtils.queryEntities(em, Event.class, queryParameters);

        return events;
    }

    public List<Event> getLastEvents(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query("order=eventPublished desc &limit=10").defaultOffset(0)
                .build();

        List<Event> events = JPAUtils.queryEntities(em, Event.class, queryParameters);

        return events;
    }

    public List<Event> getRecommendedEventsForUser(String userId) {
        User u = eventBean.getUser(userId, true);
        TypedQuery<Event> query;
        if (u.getApartments() != null && !u.getApartments().isEmpty()){
            query = em.createNamedQuery("Event.getRecommendedForUser", Event.class);
            List<String> apartments = u.getApartments().stream().map(Apartment::getId).collect(Collectors.toCollection(ArrayList::new));
            query.setParameter("apartments", apartments);
        }
        else {
            query = em.createNamedQuery("Event.getInterestingForUser", Event.class);
        }
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    public Event getEventSimple(String eventId) {

        Event event = em.find(Event.class, eventId);

        if (event == null) {
            throw new NotFoundException();
        }

        return event;
    }

    public Event getEvent(String eventId) {

        Event event = em.find(Event.class, eventId);

        if (event == null) {
            throw new NotFoundException();
        }

        event.setApartment(eventBean.getApartment(event.getApartmentId()));
        event.setUser(eventBean.getUser(event.getUserId()));

        return event;
    }

    public Event createEvent(Event event) {

        try {
            beginTx();
            em.persist(event);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return event;
    }

    public Event putEvent(String eventId, Event event) {

        Event c = em.find(Event.class, eventId);

        if (c == null) {
            return null;
        }

        try {
            beginTx();
            event.setId(c.getId());
            event = em.merge(event);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return event;
    }

    public boolean deleteEvent(String eventId) {

        Event event = em.find(Event.class, eventId);

        if (event != null) {
            try {
                beginTx();
                em.remove(event);
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
        if (basePathApartment.isPresent() && apartmentId != null) {
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


    public User getUser(String userId) {
        return getUser(userId, false);
    }

    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getUserFallback")
    @Timeout
    public User getUser(String userId, boolean withApartments) {
        log.info("IN GETUSER");
        if (basePathUser.isPresent() && userId != null) {
            String simple = "simple/";
            if (withApartments)
                simple = "";
            log.info("GETTING user with ID "+userId+" and url:"+basePathUser);
            try {
                return httpClient
                        .target(basePathUser.get() + "/v1/user/" + simple + userId)
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
