package si.fri.rso.projekt.rent.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import si.fri.rso.projekt.Availability;
import si.fri.rso.projekt.services.AvailabilityBean;

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
public class AvailabilityResource {
    @Inject
    private AvailabilityBean availabilityBean;

    @Context
    private UriInfo uriInfo;


    @GET
    public Response getAvailability() {
        List<Availability> avaliabilities = availabilityBean.getAvaliabilities(uriInfo);
        return Response.ok(avaliabilities).build();
    }

    @GET
    @Path("/{availabilityId}")
    public Response getAvailability(@PathParam("availabilityId") String availabilityId) {

        Availability availability = availabilityBean.getAvailability(availabilityId);

        if (availability == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(availability).build();
    }

    @GET
    @Path("/simple/{rentId}")
    public Response getRentSimple(@PathParam("rentId") String rentId) {

        Availability availability = availabilityBean.getAvailability(rentId);

        if (availability == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(availability).build();
    }

    @POST
    public Response createRent(Availability availability) {

        if (availability.getApartmentId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            availability = availabilityBean.createAvailability(availability);
        }

        if (availability.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(availability).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(availability).build();
        }
    }

    @PUT
    @Path("{rentId}")
    public Response putRent(@PathParam("rentId") String rentId, Availability availability) {

        availability = availabilityBean.putAvailability(rentId, availability);

        if (availability == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (availability.getId() != null)
                return Response.status(Response.Status.OK).entity(availability).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{rentId}")
    public Response deleteRent(@PathParam("rentId") String rentId) {

        boolean deleted = availabilityBean.deleteAvailability(rentId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
