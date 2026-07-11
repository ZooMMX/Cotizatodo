package com.phesus.cotizatodo.web;

import com.phesus.cotizatodo.entity.User;
import com.phesus.cotizatodo.entity.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
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
 * Port del UserController de Spring MVC.
 *
 * Corrección de seguridad respecto al original: la edición de perfil y el
 * cambio de contraseña operan SIEMPRE sobre el usuario autenticado (Principal),
 * nunca sobre el username que venga en el formulario (IDOR del legado).
 */
@Path("/user")
public class UserResource {

    @Inject UserRepository users;
    @Inject SecurityIdentity identity;

    @Inject @Location("user_profile") Template userProfile;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public TemplateInstance profile(@RestQuery Boolean successfulChange,
                                    @RestQuery Boolean unsuccessfulChange,
                                    @RestQuery Boolean wrongPassword) {
        User user = users.findByUsername(identity.getPrincipal().getName());
        return userProfile
                .data("user", user)
                .data("successfulChange", Boolean.TRUE.equals(successfulChange))
                .data("unsuccessfulChange", Boolean.TRUE.equals(unsuccessfulChange))
                .data("wrongPassword", Boolean.TRUE.equals(wrongPassword));
    }

    @POST
    @Path("/update")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response update(@RestForm String fullname,
                           @RestForm String email,
                           @RestForm String city,
                           @RestForm String country) {
        User user = users.findByUsername(identity.getPrincipal().getName());

        user.setFullname(fullname);
        user.setCity(city);
        // email y country son NOT NULL: solo se sobrescriben si llegan con valor
        if (email != null && !email.isBlank())
            user.setEmail(email);
        if (country != null && !country.isBlank())
            user.setCountry(country);

        return Response.seeOther(URI.create("/user?successfulChange=true")).build();
    }

    @POST
    @Path("/updatePassword")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    public Response updatePassword(@RestForm String oldPass,
                                   @RestForm String newPass,
                                   @RestForm String reNewPass) {
        User user = users.findByUsername(identity.getPrincipal().getName());

        /* La contraseña actual debe coincidir */
        if (oldPass == null || !BcryptUtil.matches(oldPass, user.getPassword()))
            return Response.seeOther(URI.create("/user?unsuccessfulChange=true&wrongPassword=true")).build();

        /* La nueva contraseña y su confirmación deben coincidir y no estar vacías */
        if (newPass == null || newPass.isEmpty() || !newPass.equals(reNewPass))
            return Response.seeOther(URI.create("/user?unsuccessfulChange=true")).build();

        user.setPassword(BcryptUtil.bcryptHash(newPass));

        return Response.seeOther(URI.create("/user?successfulChange=true")).build();
    }
}
