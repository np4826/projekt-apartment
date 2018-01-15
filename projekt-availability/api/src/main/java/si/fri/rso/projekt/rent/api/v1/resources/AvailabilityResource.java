package si.fri.rso.projekt.rent.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import si.fri.rso.projekt.Availability;
import si.fri.rso.projekt.CutObject;
import si.fri.rso.projekt.services.AvailabilityBean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

@RequestScoped
@Path("/availability")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log
public class AvailabilityResource {
    @Inject
    private AvailabilityBean availabilityBean;

    @Context
    private UriInfo uriInfo;

    private Logger log = Logger.getLogger(AvailabilityResource.class.getName());


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
    @Path("/check")
    public Response getCheckAvailability(@QueryParam("apartmentId") String apartmentId,
                                         @QueryParam("start") String  start,
                                         @QueryParam("end") String  end) {
        Date dateStart;
        Date dateEnd;
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            dateStart = format.parse(start);
            dateEnd = format.parse(end);
            Boolean rezultat = availabilityBean.getCheckAvailability(apartmentId,dateStart,dateEnd);
            return Response.status(Response.Status.OK).entity(rezultat).build();
        } catch(Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/cut")
    public Response postAvailabilityCut(CutObject input) {
        log.info("IN CUT METHOD");
        Date dateStart;
        Date dateEnd;
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            dateStart = format.parse(input.start);
            dateEnd = format.parse(input.end);
            availabilityBean.AvailabilityCut(input.apartmentId,dateStart,dateEnd);
            return Response.status(Response.Status.OK).entity(true).build();
        } catch(Exception e) {
            log.info("ERROR IN CUT METHOD");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

    }

    @GET
    @Path("/simple/{availabilityId}")
    public Response getAvailabilitySimple(@PathParam("availabilityId") String availabilityId) {

        Availability availability = availabilityBean.getAvailability(availabilityId);

        if (availability == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(availability).build();
    }

    @POST
    public Response createAvailability(Availability availability) {

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
    @Path("{availabilityId}")
    public Response putAvailability(@PathParam("availabilityId") String availabilityId, Availability availability) {

        availability = availabilityBean.putAvailability(availabilityId, availability);

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
    @Path("{availabilityId}")
    public Response deleteAvailability(@PathParam("availabilityId") String availabilityId) {

        boolean deleted = availabilityBean.deleteAvailability(availabilityId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
