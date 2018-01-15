package si.fri.rso.projekt.payment.api.v1.resources;

import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.logs.cdi.LogParams;
import org.eclipse.microprofile.metrics.annotation.Metered;
import si.fri.rso.projekt.Payment;
import si.fri.rso.projekt.services.PaymentBean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@RequestScoped
@Path("/payment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Log(LogParams.METRICS)
public class PaymentResource {
    @Context
    private UriInfo uriInfo;

    @Inject
    private PaymentBean paymentBean;

    @GET
    @Metered
    public Response getPayments() {
        List<Payment> payments = paymentBean.getPayments(uriInfo);
        return Response.ok(payments).build();
    }

    @GET
    @Path("/{paymentId}")
    public Response getPayment(@PathParam("paymentId") String paymentId) {

        Payment payment = paymentBean.getPayment(paymentId);

        if (payment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(payment).build();
    }

    @POST
    public Response createPayment(Payment payment) {
        payment = paymentBean.createPayment(payment);

        if (payment.getId() != null) {
            return Response.status(Response.Status.CREATED).entity(payment).build();
        } else {
            return Response.status(Response.Status.CONFLICT).entity(payment).build();
        }
    }

    @PUT
    @Path("{paymentId}")
    public Response putPayment(@PathParam("paymentId") String paymentId, Payment payment) {

        payment = paymentBean.putPayment(paymentId, payment);

        if (payment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            if (payment.getId() != null)
                return Response.status(Response.Status.OK).entity(payment).build();
            else
                return Response.status(Response.Status.NOT_MODIFIED).build();
        }
    }

    @DELETE
    @Path("{paymentId}")
    public Response deletePayment(@PathParam("paymentId") String paymentId) {

        boolean deleted = paymentBean.deletePayment(paymentId);

        if (deleted) {
            return Response.status(Response.Status.GONE).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
