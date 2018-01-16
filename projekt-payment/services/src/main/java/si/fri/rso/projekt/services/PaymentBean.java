package si.fri.rso.projekt.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import si.fri.rso.projekt.Payment;
import si.fri.rso.projekt.Rent;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PaymentBean {
    @Inject
    private EntityManager em;
    private Logger log = LogManager.getLogger(PaymentBean.class.getName());
    private ObjectMapper objectMapper;

    private Client httpClient;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        objectMapper = new ObjectMapper();
    }

    @Inject
    @DiscoverService(value = "rso-rent")
    private Optional<String> basePathRent;

    public List<Payment> getPayments(UriInfo uriInfo) {

        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery())
                .defaultOffset(0)
                .build();

        List<Payment> Payments = JPAUtils.queryEntities(em, Payment.class, queryParameters);
        for (Payment p : Payments) {
            AddRentToPayment(p);
        }

        return Payments;
    }

    public Payment getPayment(String paymentId) {

        Payment payment = em.find(Payment.class, paymentId);

        if (payment == null) {
            throw new NotFoundException();
        }

        AddRentToPayment(payment);

        return payment;
    }

    private void AddRentToPayment(Payment payment) {
        if (basePathRent.isPresent()) {
            log.info("IN SETRENT BASEPATH");
            String request = basePathRent.get()+"/v1/rent/"+ payment.getRentId();
            log.info("CALLING "+request);
            try {
                payment.setRent(httpClient
                        .target(request)
                        .request().get(new GenericType<Rent>() {
                        }));
            } catch (WebApplicationException | ProcessingException e) {
                log.error(e.getMessage());
                log.error(e);
                Rent rent = new Rent();
                rent.setComment("ERROR ON GETTING RENT FOR THIS PAYMENT");
                payment.setRent(rent);
            }
        }
    }



    public Payment createPayment(Payment payment) {
        try {
            beginTx();
            em.persist(payment);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return payment;
    }

    public Payment putPayment(String paymentId, Payment payment) {

        Payment c = em.find(Payment.class, paymentId);

        if (c == null) {
            return null;
        }

        try {
            beginTx();
            payment.setId(c.getId());
            payment = em.merge(payment);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return payment;
    }

    public boolean deletePayment(String paymentId) {

        Payment payment = em.find(Payment.class, paymentId);

        if (payment != null) {
            try {
                beginTx();
                em.remove(payment);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else
            return false;

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }
}
