package si.fri.rso.projekt.apartment.api.v1.resources;

import si.fri.rso.projekt.Apartment;
import si.fri.rso.projekt.services.ApartmentBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@ApplicationScoped
@Path("/apartment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApartmentResource {
    @Context
    private UriInfo uriInfo;

    @Inject
    private ApartmentBean apartmentBean;

    @GET
    public Response getApartments() {
        List<Apartment> apartments = apartmentBean.getApartments(uriInfo);
        return Response.ok(apartments).build();
    }

    @GET
    @Path("/{apartmentId}")
    public Response getApartment(@PathParam("apartmentId") String apartmentId) {

        Apartment apartment = apartmentBean.getApartment(apartmentId);

        if (apartment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(apartment).build();
    }

    @POST
    public Response createApartment(Apartment apartment) {

        if (apartment.getTitle() == null || apartment.getTitle().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            apartment = apartmentBean.createApartment(apartment);
        }

        if (apartment.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(apartment).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(apartment).build();
        }
    }

    @PUT
    @Path("{apartmentId}")
    public Response putApartment(@PathParam("apartmentId") String apartmentId, Apartment apartment) {

        apartment = apartmentBean.putApartment(apartmentId, apartment);

        if (apartment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (apartment.getId() != null)
                return Response.status(Response.Status.OK).entity(apartment).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{apartmentId}")
    public Response deleteApartment(@PathParam("apartmentId") String apartmentId) {

        boolean deleted = apartmentBean.deleteApartment(apartmentId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
