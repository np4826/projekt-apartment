package si.fri.rso.projekt.recommendation.api.v1.mappers;

import si.fri.rso.projekt.recommendation.api.v1.dtos.ApiError;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(NotFoundException e) {

        ApiError apiError = new ApiError();
        apiError.setStatus(404);
        apiError.setCode("resource.not.found");
        apiError.setMessage(e.getMessage());

        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(apiError)
                .build();
    }
}