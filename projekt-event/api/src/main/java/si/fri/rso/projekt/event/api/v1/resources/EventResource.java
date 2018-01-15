package si.fri.rso.projekt.event.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import si.fri.rso.projekt.Event;
import si.fri.rso.projekt.services.EventBean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@RequestScoped
@Path("/event")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log
public class EventResource {
    @Inject
    private EventBean eventBean;

    @Context
    private UriInfo uriInfo;


    @GET
    public Response getEvents() {
        //http://localhost:8086/v1/event?filter=apartmentId:EQ:2
        List<Event> events = eventBean.getEvents(uriInfo);
        return Response.ok(events).build();
    }

    @GET
    @Path("/{eventId}")
    public Response getEvent(@PathParam("eventId") String eventId) {

        Event event = eventBean.getEvent(eventId);

        if (event == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(event).build();
    }

    @GET
    @Path("/simple/{eventId}")
    public Response getEventSimple(@PathParam("eventId") String eventId) {

        Event event = eventBean.getEventSimple(eventId);

        if (event == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(event).build();
    }

    @GET
    @Path("/last")
    public Response getLastEvents() {

        List<Event> events;

        events = eventBean.getLastEvents(uriInfo);

        return Response.status(Response.Status.OK).entity(events).build();
    }

    @GET
    @Path("/recommended/{userId}")
    public Response getRecommendedEventsForUser(@PathParam("userId") String userId) {

        List<Event> events;

        events = eventBean.getRecommendedEventsForUser(userId);

        return Response.status(Response.Status.OK).entity(events).build();
    }


    @POST
    public Response createEvent(Event event) {

        if (event.getUserId() == null || event.getApartmentId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        else {
            event = eventBean.createEvent(event);
        }

        if (event.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(event).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(event).build();
        }
    }

    @PUT
    @Path("{eventId}")
    public Response putEvent(@PathParam("eventId") String eventId, Event event) {

        event = eventBean.putEvent(eventId, event);

        if (event == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (event.getId() != null)
                return Response.status(Response.Status.OK).entity(event).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{eventId}")
    public Response deleteEvent(@PathParam("eventId") String eventId) {

        boolean deleted = eventBean.deleteEvent(eventId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
