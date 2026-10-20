package com.example.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String error = "Internal Server Error";

        if (exception instanceof WebApplicationException webAppException) {
            status = webAppException.getResponse().getStatus();
            error = webAppException.getMessage();
        }
        else if (exception.getClass().getName().contains("ConstraintViolationException")) {
            status = Response.Status.CONFLICT.getStatusCode();
            error = "Нарушение целостности данных в базе (возможно, запись связана с другими таблицами)";
        }

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of(
                        "status", status,
                        "error", error,
                        "message", exception.getMessage() != null ? exception.getMessage() : "Детали отсутствуют"
                ))
                .build();
    }
}