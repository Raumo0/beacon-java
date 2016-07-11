package com.dnastack.beacon.rest.sys;

import com.dnastack.beacon.rest.exceptions.BeaconException;
import com.dnastack.beacon.rest.exceptions.InvalidAlleleRequestException;
import org.ga4gh.beacon.BeaconAlleleResponse;
import org.ga4gh.beacon.BeaconError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * @author patmagee
 * @author Artem (tema.voskoboynick@gmail.com)
 * @version 1.0
 */
@Provider
public class BeaconExceptionHandler implements ExceptionMapper<BeaconException> {

    /**
     * Default error handler to capture all errors and send the user a JSON response
     * with the status code, reason, message and stacktrace of the error
     *
     * @return response with the status and error entity
     */
    @Override
    public Response toResponse(BeaconException exception) {
        if (exception instanceof InvalidAlleleRequestException) {
            return handleInvalidAlleleRequestException((InvalidAlleleRequestException) exception);
        } else {
            return handleDefaultBeaconException(exception);
        }
    }

    private Response handleInvalidAlleleRequestException(InvalidAlleleRequestException exception) {
        BeaconAlleleResponse response = new BeaconAlleleResponse();
        response.setAlleleRequest(exception.getRequest());
        response.setExists(null);

        BeaconError error = BeaconError.newBuilder()
                .setErrorCode(BAD_REQUEST.getStatusCode())
                .setMessage(exception.getMessage())
                .build();
        response.setError(error);

        return Response.status(error.getErrorCode()).entity(response).build();
    }

    private Response handleDefaultBeaconException(BeaconException exception) {
        BeaconError response = BeaconError.newBuilder()
                .setMessage(exception.getMessage())
                .setErrorCode(INTERNAL_SERVER_ERROR.getStatusCode())
                .build();

        return Response.status(response.getErrorCode()).entity(response).build();
    }
}