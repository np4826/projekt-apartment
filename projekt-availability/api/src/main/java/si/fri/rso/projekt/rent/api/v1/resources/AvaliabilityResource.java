package si.fri.rso.projekt.rent.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import si.fri.rso.projekt.Avaliability;
import si.fri.rso.projekt.services.AvaliabilityBean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@RequestScoped
@Path("/rent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log
public class AvaliabilityResource {
    @Inject
    private AvaliabilityBean avaliabilityBean;

    @Context
    private UriInfo uriInfo;


    @GET
    public Response getAvaliability() {
        List<Avaliability> avaliabilities = avaliabilityBean.getAvaliabilities(uriInfo);
        return Response.ok(avaliabilities).build();
    }

    @GET
    @Path("/{avaliabilityId}")
    public Response getAvaliability(@PathParam("avaliabilityId") String avaliabilityId) {

        Avaliability avaliability = avaliabilityBean.getAvaliability(avaliabilityId);

        if (avaliability == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(avaliability).build();
    }

    @GET
    @Path("/simple/{rentId}")
    public Response getRentSimple(@PathParam("rentId") String rentId) {

        Avaliability avaliability = avaliabilityBean.getAvaliability(rentId);

        if (avaliability == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(avaliability).build();
    }

    @POST
    public Response createRent(Avaliability avaliability) {

        if (avaliability.getApartmentId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            avaliability = avaliabilityBean.createAvaliability(avaliability);
        }

        if (avaliability.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(avaliability).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(avaliability).build();
        }
    }

    @PUT
    @Path("{rentId}")
    public Response putRent(@PathParam("rentId") String rentId, Avaliability avaliability) {

        avaliability = avaliabilityBean.putAvaliability(rentId, avaliability);

        if (avaliability == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (avaliability.getId() != null)
                return Response.status(Response.Status.OK).entity(avaliability).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{rentId}")
    public Response deleteRent(@PathParam("rentId") String rentId) {

        boolean deleted = avaliabilityBean.deleteAvaliability(rentId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
