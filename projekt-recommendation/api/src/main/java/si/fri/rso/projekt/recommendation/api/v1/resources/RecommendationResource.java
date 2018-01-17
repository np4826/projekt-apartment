package si.fri.rso.projekt.recommendation.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import si.fri.rso.projekt.Recommendation;
import si.fri.rso.projekt.services.RecommendationBean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@RequestScoped
@Path("/recommendation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log
public class RecommendationResource {
    @Inject
    private RecommendationBean recommendationBean;

    @Context
    private UriInfo uriInfo;


    @GET
    public Response getRecommendations() {
        List<Recommendation> recommendations = recommendationBean.getRecommendations(uriInfo);
        return Response.ok(recommendations).build();
    }

    @GET
    @Path("/{recommendationId}")
    public Response getRecommendation(@PathParam("recommendationId") String recommendationId) {

        Recommendation recommendation = recommendationBean.getRecommendation(recommendationId);

        if (recommendation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(recommendation).build();
    }

    @GET
    @Path("/simple/{recommendationId}")
    public Response getRecommendationSimple(@PathParam("recommendationId") String recommendationId) {

        Recommendation recommendation = recommendationBean.getRecommendationSimple(recommendationId);

        if (recommendation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(recommendation).build();
    }

    @GET
    @Path("/user/{userId}")
    public Response getNewRecommendationForUser(@PathParam("userId") String userId) {

        Recommendation recommendation = recommendationBean.getNewRecommendationForUser(userId);

        if (recommendation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(recommendation).build();
    }


    // delete?
    @POST
    public Response createRecommendation(Recommendation recommendation) {

        if (recommendation.getUserId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        /*else if (recommendation.getUserId().equals(recommendation.getApartment().getUserId())){ // can't recommendation your own apartment
            return Response.status(Response.Status.CONFLICT).build();
        }*/
        else {
            recommendation = recommendationBean.createRecommendation(recommendation);
        }
        if (recommendation.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(recommendation).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(recommendation).build();
        }
    }

    @PUT
    @Path("{recommendationId}")
    public Response putRecommendation(@PathParam("recommendationId") String recommendationId, Recommendation recommendation) {

        recommendation = recommendationBean.putRecommendation(recommendationId, recommendation);

        if (recommendation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (recommendation.getId() != null)
                return Response.status(Response.Status.OK).entity(recommendation).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{recommendationId}")
    public Response deleteRecommendation(@PathParam("recommendationId") String recommendationId) {

        boolean deleted = recommendationBean.deleteRecommendation(recommendationId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
