package si.fri.rso.projekt.rent.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import si.fri.rso.projekt.Rent;
import si.fri.rso.projekt.services.RentBean;

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
public class RentResource {
    @Inject
    private RentBean rentBean;

    @Context
    private UriInfo uriInfo;


    @GET
    public Response getRents() {
        List<Rent> rents = rentBean.getRents(uriInfo);
        return Response.ok(rents).build();
    }

    @GET
    @Path("/{rentId}")
    public Response getRent(@PathParam("rentId") String rentId) {

        Rent rent = rentBean.getRent(rentId);

        if (rent == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(rent).build();
    }

    @GET
    @Path("/simple/{rentId}")
    public Response getRentSimple(@PathParam("rentId") String rentId) {

        Rent rent = rentBean.getRent(rentId);

        if (rent == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(rent).build();
    }

    @POST
    public Response createRent(Rent rent) {

        if (rent.getUserId() == null || rent.getApartmentId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            rent = rentBean.createRent(rent);
        }

        if (rent.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(rent).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(rent).build();
        }
    }

    @PUT
    @Path("{rentId}")
    public Response putRent(@PathParam("rentId") String rentId, Rent rent) {

        rent = rentBean.putRent(rentId, rent);

        if (rent == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (rent.getId() != null)
                return Response.status(Response.Status.OK).entity(rent).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{rentId}")
    public Response deleteRent(@PathParam("rentId") String rentId) {

        boolean deleted = rentBean.deleteRent(rentId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
