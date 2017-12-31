package si.fri.rso.projekt.review.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
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

    @Context
    private UriInfo uriInfo;


    @GET
    public Response getReviews() {
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

    @POST
    public Response createReview(Review review) {

        if (review.getUserId() == null || review.getApartmentId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        /*else if (review.getUserId().equals(review.getApartment().getUserId())){ // can't review your own apartment
            return Response.status(Response.Status.CONFLICT).build();
        }*/
        else {
            review = reviewBean.createReview(review);
        }

        if (review.getId() != null) {
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
