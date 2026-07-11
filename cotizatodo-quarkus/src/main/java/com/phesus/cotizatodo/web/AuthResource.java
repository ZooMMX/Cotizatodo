package com.phesus.cotizatodo.web;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.net.URI;

/**
 * Cierre de sesión: expira la cookie cifrada de form-auth y regresa al login,
 * replicando el circuito /logout -> /login?logout de la app legada.
 */
@Path("/logout")
public class AuthResource {

    @POST
    public Response logout() {
        NewCookie expired = new NewCookie.Builder("quarkus-credential")
                .value("")
                .path("/")
                .maxAge(0)
                .build();
        return Response.seeOther(URI.create("/login?logout")).cookie(expired).build();
    }
}
