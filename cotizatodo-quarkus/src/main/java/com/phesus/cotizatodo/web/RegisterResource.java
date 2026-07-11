package com.phesus.cotizatodo.web;

import com.phesus.cotizatodo.entity.User;
import com.phesus.cotizatodo.entity.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import java.net.URI;

/**
 * Port del RegisterController.
 *
 * Corrección de seguridad respecto al original: se enlazan únicamente los
 * campos permitidos (nada de mass assignment de roles/enabled) y se valida
 * la disponibilidad del username en servidor.
 */
@Path("/")
public class RegisterResource {

    @Inject UserRepository users;

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response register(@RestForm String fullname,
                             @RestForm String email,
                             @RestForm String city,
                             @RestForm String country,
                             @RestForm String username,
                             @RestForm String password) {

        if (username == null || username.isBlank()
                || password == null || password.isEmpty()
                || email == null || email.isBlank()) {
            return Response.seeOther(URI.create("/login?register&error=invalid")).build();
        }
        if (users.exists(username.trim())) {
            return Response.seeOther(URI.create("/login?register&error=userexists")).build();
        }

        User user = new User(username.trim(), BcryptUtil.bcryptHash(password), true);
        user.setFullname(fullname);
        user.setEmail(email);
        user.setCity(city);
        user.setCountry(country != null && !country.isBlank() ? country : "XX");
        user.addUserRole("ROLE_USER");

        users.persist(user);

        return Response.seeOther(URI.create("/login")).build();
    }

    @GET
    @Path("usuarioCheckUsername")
    @Produces(MediaType.TEXT_PLAIN)
    public String checkUsername(@RestQuery String username) {
        if (username == null)
            return "nombre inválido";
        return String.valueOf(!users.exists(username));
    }
}
