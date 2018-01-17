package si.fri.rso.projekt.review.api.v1.resources;

import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.logs.cdi.Log;
import si.fri.rso.projekt.Apartment;
import si.fri.rso.projekt.Message;
import si.fri.rso.projekt.Rent;
import si.fri.rso.projekt.Review;
import si.fri.rso.projekt.services.ReviewBean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@RequestScoped
@Path("/review")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log
public class ReviewResource {
    @Inject
    private ReviewBean reviewBean;

    @Inject
    private ProducerResource producerResource;

    @Context
    private UriInfo uriInfo;

    private Logger log = LogManager.getLogger(ReviewBean.class.getName());


    @GET
    public Response getReviews() {
        List<Review> reviews = reviewBean.getReviews(uriInfo);
        return Response.ok(reviews).build();
    }

    @GET
    @Path("/simple/")
    public Response getReviewsSimple() {
        List<Review> reviews = reviewBean.getReviews(uriInfo);
        return Response.ok(reviews).build();
    }

    @GET
    @Path("/{reviewId}")
    public Response getReview(@PathParam("reviewId") String reviewId) {

        Review review = reviewBean.getReview(reviewId);

        if (review == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(review).build();
    }

    @GET
    @Path("/simple/{reviewId}")
    public Response getReviewSimple(@PathParam("reviewId") String reviewId) {

        Review review = reviewBean.getReviewSimple(reviewId);

        if (review == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(review).build();
    }

    @GET
    @Path("/filtered")
    public Response getReviewsFiltered() {
        //http://localhost:8085/v1/review/filtered?filter=apartmentId:EQ:1
        List<Review> reviews;

        reviews = reviewBean.getReviewsFilter(uriInfo);

        return Response.status(Response.Status.OK).entity(reviews).build();
    }

    @GET
    @Path("/bestRated")
    public Response getReviewsBestRated() {

        List<Review> reviews;

        reviews = reviewBean.getReviewsBestRated(null);

        return Response.status(Response.Status.OK).entity(reviews).build();
    }

    @GET
    @Path("/bestRatedWithoutOwner/{ownerId}")
    public Response getReviewsBestRatedWithoutOwner(@PathParam("ownerId") String ownerId) {

        List<Review> reviews;

        reviews = reviewBean.getReviewsBestRated(ownerId);

        return Response.status(Response.Status.OK).entity(reviews).build();
    }


    @POST
    public Response createReview(Review review) {
        if (review.getUserId().isEmpty() || review.getApartmentId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }


        List<Rent> rents = reviewBean.getRent(review.getUserId(),review.getApartmentId());
        if (rents.isEmpty()){
            log.info("CreateReview: cannot review if not rented before");
            return Response.status(Response.Status.CONFLICT).build();
        } else if(rents.get(0).getComment().equals("N/A"))
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();

        Apartment apartment =  reviewBean.getApartment(review.getApartmentId());
        if (apartment.getTitle().equals("N/A"))
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();

        if (review.getUserId().equals(apartment.getUserId())){
            log.info("CreateReview: can't review your own apartment");
            return Response.status(Response.Status.CONFLICT).build();
        } else if (reviewBean.existsReview(review.getUserId(),review.getApartmentId())){
            log.info("CreateReview: user can review one apartment only once");
            return Response.status(Response.Status.CONFLICT).build();
        }
        else {
            review = reviewBean.createReview(review);
        }
        if (review.getId() != null) {
            String content = "New rating submitted on apartment";
            if (review.getComment() != null)
                content = "New review on apartment";
            producerResource.produceMessage(new Message(review.getApartmentId(), review.getUserId(), content));
            return Response.status(Response.Status.CREATED).entity(review).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(review).build();
        }
    }

    @PUT
    @Path("{reviewId}")
    public Response putReview(@PathParam("reviewId") String reviewId, Review review) {

        review = reviewBean.putReview(reviewId, review);

        if (review == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (review.getId() != null)
                return Response.status(Response.Status.OK).entity(review).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{reviewId}")
    public Response deleteReview(@PathParam("reviewId") String reviewId) {

        boolean deleted = reviewBean.deleteReview(reviewId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
