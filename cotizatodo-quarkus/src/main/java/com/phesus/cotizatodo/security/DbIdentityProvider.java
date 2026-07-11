package com.phesus.cotizatodo.security;

import com.phesus.cotizatodo.entity.User;
import com.phesus.cotizatodo.entity.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Autenticación usuario/contraseña contra la tabla `user` (hash BCrypt),
 * equivalente al MyUserDetailsService + BCryptPasswordEncoder de Spring.
 */
@ApplicationScoped
public class DbIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    @Inject
    UserRepository users;

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
                                              AuthenticationRequestContext context) {
        return context.runBlocking(() -> authenticateBlocking(request));
    }

    @Transactional
    SecurityIdentity authenticateBlocking(UsernamePasswordAuthenticationRequest request) {
        User user = users.findByUsername(request.getUsername());
        if (user == null || !user.isEnabled())
            throw new AuthenticationFailedException();

        String password = new String(request.getPassword().getPassword());
        if (!BcryptUtil.matches(password, user.getPassword()))
            throw new AuthenticationFailedException();

        return IdentityBuilder.from(user);
    }
}
