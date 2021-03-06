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
import si.fri.rso.projekt.CutObject;
import si.fri.rso.projekt.Rent;
import si.fri.rso.projekt.User;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RequestScoped
public class RentBean {

    private Logger log = LogManager.getLogger(RentBean.class.getName());

    @Inject
    private EntityManager em;

    private ObjectMapper objectMapper;

    private Client httpClient;

    @Inject
    private RentBean rentBean;

    @Inject
    @DiscoverService(value = "rso-apartment")
    private Optional<String> basePathApartment;

    @Inject
    @DiscoverService(value = "rso-user")
    private Optional<String> basePathUser;

    @Inject
    @DiscoverService(value = "rso-availability")
    private Optional<String> basePathAvailability;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        objectMapper = new ObjectMapper();
    }

    public List<Rent> getRents(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();


        List<Rent> rents = JPAUtils.queryEntities(em, Rent.class, queryParameters);
        for(Rent r : rents)
        {
            r.setApartment(rentBean.getApartment(r.getApartmentId()));
            r.setUser(rentBean.getUser(r.getUserId()));
        }
        return rents;
    }

    public List<Rent> getRentsSimple(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();

        List<Rent> rents = JPAUtils.queryEntities(em, Rent.class, queryParameters);
        return rents;
    }

    public Rent getRent(String rentId) {

        Rent rent = em.find(Rent.class, rentId);
        rent.setApartment(getApartment(rent.getApartmentId()));
        rent.setUser(getUser(rent.getUserId()));

        if (rent == null) {
            throw new NotFoundException();
        }

        return rent;
    }

    public Boolean checkRent(Rent rent){
        if (basePathAvailability.isPresent()) {
            log.info("BASEPATH for AVAILABILITY is present");
            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                Boolean check = httpClient
                        .target(basePathAvailability.get() + "/v1/availability/check?apartmentId="+rent.getApartmentId()+
                                "&start=" + df.format(rent.getRentStart()).toString() +
                                "&end=" + df.format(rent.getRentEnd()).toString())
                        .request().get(new GenericType<Boolean>() {
                        });

                log.info("RENT AVAILABILITY TEST 1 RETURNED "+ check);

                List<Rent> test = em.createNamedQuery("Rent.getByApartmentAndDates")
                        .setParameter("apartmentId",rent.getApartmentId())
                        .setParameter("rStart", rent.getRentStart())
                        .setParameter("rEnd",rent.getRentEnd())
                        .getResultList();

                log.info("RENT AVAILABILITY TEST 2 RETURNED "+test.size()+" RESULTS");

                return check && test.size()==0;
            } catch (WebApplicationException | ProcessingException e) {
                log.info("ERROR IN CHECK RENT");
                log.error(e);
                throw new InternalServerErrorException(e);
            }
        }
        log.info("BASEPATH for AVAILABILITY is not present");
        return false;
    }

    public void afterCreateRent(Rent rent){
        log.info("RENT - CUTTING AVAILABILITY BY RENT "+basePathAvailability.isPresent());
        if (basePathAvailability.isPresent()) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            CutObject cutObject = new CutObject();
            cutObject.apartmentId = rent.getApartmentId();
            cutObject.start = df.format(rent.getRentStart()).toString();
            cutObject.end = df.format(rent.getRentEnd()).toString();

            log.info("RENT - CALLING CUT METHOD");
            log.info("RENT - METHOD CUT ON "+basePathAvailability.get() + "/v1/availability/cut");

            try{
                Response cutResult = httpClient
                        .target(basePathAvailability.get() + "/v1/availability/cut")
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.json(cutObject));

                log.info("RENT - CUT COMPLETED WITH STATUS "+cutResult.getStatus());
            }
            catch (Exception e){
                log.error(e.getMessage());
            }
        }
    }

    public List<Rent> getOtherRentsForApartment(String userId, String apartmentId) {
        TypedQuery<Rent> query = em.createNamedQuery("Rent.getOtherRentsForApartment", Rent.class);;
        query.setParameter("userId", userId);
        query.setParameter("apartmentId", apartmentId);
        List<Rent> others = query.getResultList();
        List<String> users = others.stream().map(Rent::getUserId).collect(Collectors.toCollection(ArrayList::new));

        if (!others.isEmpty()){
            query = em.createNamedQuery("Rent.getOtherSimilarRents", Rent.class);
            query.setParameter("userId", users);
            query.setParameter("apartmentId", apartmentId);

            return query.getResultList();
        }
        return others;
    }

    public Rent createRent(Rent rent) {
        try {
            beginTx();
            log.info("CREATING NEW RENT");
            em.persist(rent);
            afterCreateRent(rent);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return rent;
    }

    public Rent putRent(String rentId, Rent rent) {

        Rent c = em.find(Rent.class, rentId);

        if (c == null) {
            return null;
        }

        try {
            beginTx();
            rent.setId(c.getId());
            rent = em.merge(rent);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return rent;
    }

    public boolean deleteRent(String rentId) {

        Rent rent = em.find(Rent.class, rentId);

        if (rent != null) {
            try {
                beginTx();
                em.remove(rent);
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
            log.info("BASEPATH for USER is present");
            log.info("GETTING user with ID "+userId);
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
