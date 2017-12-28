package si.fri.rso.projekt.user.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.logs.cdi.LogParams;
import org.eclipse.microprofile.metrics.annotation.Metered;
import si.fri.rso.projekt.User;
import si.fri.rso.projekt.services.UserBean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@RequestScoped
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log(LogParams.METRICS)
public class UserResource {

    @Inject
    private UserBean userBean;

    @Context
    private UriInfo uriInfo;


    @GET
    @Metered
    public Response getUsers() {
        List<User> users = userBean.getUsersWithApartments(uriInfo);
        return Response.ok(users).build();
    }

    @GET
    @Path("/{userId}")
    @Metered
    public Response getUser(@PathParam("userId") String userId) {

        User user = userBean.getUser(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(user).build();
    }

    @GET
    @Path("/simple/{userId}")
    @Metered
    public Response getUserSimple(@PathParam("userId") String userId) {

        User user = userBean.getUser(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(user).build();
    }

    @POST
    public Response createUser(User user) {

        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            user = userBean.createUser(user);
        }

        if (user.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(user).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(user).build();
        }
    }

    @PUT
    @Path("{userId}")
    public Response putUser(@PathParam("userId") String userId, User user) {

        user = userBean.putUser(userId, user);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (user.getId() != null)
                return Response.status(Response.Status.OK).entity(user).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{userId}")
    public Response deleteUser(@PathParam("userId") String userId) {

        boolean deleted = userBean.deleteUser(userId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
