package com.phesus.cotizatodo.security;

import com.phesus.cotizatodo.entity.User;
import com.phesus.cotizatodo.entity.UserRepository;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TrustedAuthenticationRequest;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Re-autenticación por cookie de sesión de form-auth: Quarkus valida la
 * cookie cifrada y nos pide reconstruir la identidad a partir del username.
 */
@ApplicationScoped
public class DbTrustedIdentityProvider implements IdentityProvider<TrustedAuthenticationRequest> {

    @Inject
    UserRepository users;

    @Override
    public Class<TrustedAuthenticationRequest> getRequestType() {
        return TrustedAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(TrustedAuthenticationRequest request,
                                              AuthenticationRequestContext context) {
        return context.runBlocking(() -> authenticateBlocking(request));
    }

    @Transactional
    SecurityIdentity authenticateBlocking(TrustedAuthenticationRequest request) {
        User user = users.findByUsername(request.getPrincipal());
        if (user == null || !user.isEnabled())
            throw new AuthenticationFailedException();

        return IdentityBuilder.from(user);
    }
}
