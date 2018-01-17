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
import si.fri.rso.projekt.Rent;
import si.fri.rso.projekt.Review;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RequestScoped
public class ReviewBean {

    private Logger log = LogManager.getLogger(ReviewBean.class.getName());

    @Inject
    private EntityManager em;


    private Client httpClient;

    @Inject
    private ReviewBean reviewBean;

    @Inject
    @DiscoverService(value = "rso-apartment")
    private Optional<String> basePathApartment;

    @Inject
    @DiscoverService(value = "rso-user")
    private Optional<String> basePathUser;

    @Inject
    @DiscoverService(value = "rso-rent")
    private Optional<String> basePathRent;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
    }

    public List<Review> getReviews(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();


        List<Review> reviews = JPAUtils.queryEntities(em, Review.class, queryParameters);
        for(Review r : reviews)
        {
            r.setApartment(reviewBean.getApartment(r.getApartmentId()));
            r.setUser(reviewBean.getUser(r.getUserId()));
        }
        return reviews;
    }

    public List<Review> getReviewsFilter(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        List<Review> reviews = JPAUtils.queryEntities(em, Review.class, queryParameters);

        return reviews;
    }


    public List<Review> getReviewsBestRated(String ownerId) {
        //localhost:8085/v1/review/filtered?filter=userId:NEQ:3&order=rating%20desc,reviewPublished%20desc
        String query = "order=rating desc,reviewPublished desc&limit=10";
        if (ownerId != null)
            query = "filter=userId:NEQ:" + ownerId + "&" + query;
        QueryParameters queryParameters = QueryParameters.query(query).defaultOffset(0)
                .build();

        List<Review> reviews = JPAUtils.queryEntities(em, Review.class, queryParameters);

        return reviews;
    }

    public Review getReviewSimple(String reviewId) {

        Review review = em.find(Review.class, reviewId);

        if (review == null) {
            throw new NotFoundException();
        }

        return review;
    }

    public Review getReview(String reviewId) {

        Review review = em.find(Review.class, reviewId);

        if (review == null) {
            throw new NotFoundException();
        }

        review.setApartment(reviewBean.getApartment(review.getApartmentId()));
        review.setUser(reviewBean.getUser(review.getUserId()));

        return review;
    }

    public boolean existsReview(String userId, String apartmentId){
        String query = "filter=userId:EQ:" +userId+" apartmentId:EQ:"+apartmentId;
        QueryParameters queryParameters = QueryParameters.query(query).defaultOffset(0)
                .build();

        List<Review> reviews = JPAUtils.queryEntities(em, Review.class, queryParameters);

        return !reviews.isEmpty();
    }

    public Review createReview(Review review) {

        try {
            beginTx();
            em.persist(review);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return review;
    }

    public Review putReview(String reviewId, Review review) {

        Review c = em.find(Review.class, reviewId);

        if (c == null) {
            return null;
        }

        try {
            beginTx();
            review.setId(c.getId());
            review = em.merge(review);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return review;
    }

    public boolean deleteReview(String reviewId) {

        Review review = em.find(Review.class, reviewId);

        if (review != null) {
            try {
                beginTx();
                em.remove(review);
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
    @Fallback(fallbackMethod = "getRentFallback")
    @Timeout
    public List<Rent> getRent(String userId, String apartmentId) {
        log.info("IN GETRENT:"+basePathRent);
        if (basePathRent.isPresent()) {
            try {//http://localhost:8083/v1/rent/simple?filter=userId:EQ:3%20apartmentId:EQ:2
                return httpClient
                        .target(basePathRent.get() + "/v1/rent/simple?filter=userId:EQ:" + userId+"%20apartmentId:EQ:"+apartmentId)
                        .request().get(new GenericType<List<Rent>>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.info("ERROR IN GETRENT");
                log.error(e);
                throw new InternalServerErrorException(e);
            }
        }
        log.info("BASEPATH for RENT is not present");
        return null;
    }

    public List<Rent>  getRentFallback(String userId, String apartmentId) {
        log.info("IN GETRENT FALLBACK");
        List<Rent>  rents= new ArrayList<>();
        Rent rent = new Rent();
        rent.setComment("N/A");
        rents.add(rent);
        return rents;
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
