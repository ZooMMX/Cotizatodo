package com.phesus.cotizatodo.web;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/**
 * Páginas de error 403/404, equivalentes a las del HomeController legado.
 */
public class ErrorMappers {

    @Inject @Location("403") Template forbidden;
    @Inject @Location("404") Template notFound;

    @ServerExceptionMapper
    public Response forbidden(ForbiddenException e) {
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.TEXT_HTML)
                .entity(forbidden.instance().render())
                .build();
    }

    @ServerExceptionMapper
    public Response notFound(NotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.TEXT_HTML)
                .entity(notFound.instance().render())
                .build();
    }
}
