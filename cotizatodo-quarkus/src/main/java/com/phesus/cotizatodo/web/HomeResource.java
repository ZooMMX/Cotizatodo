package com.phesus.cotizatodo.web;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestQuery;

/**
 * Port del HomeController: home, login y página 403.
 */
@Path("/")
public class HomeResource {

    @Inject Template home;
    @Inject Template login;
    @Inject @Location("403") Template forbidden;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance home() {
        return home.instance();
    }

    @GET
    @Path("login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance login(@RestQuery("error") String error,
                                  @RestQuery("logout") String logout,
                                  @RestQuery("register") String register) {
        return login
                .data("error", error != null)
                .data("logout", logout != null)
                .data("register", register != null);
    }

    @GET
    @Path("403")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance forbidden() {
        return forbidden.instance();
    }
}
