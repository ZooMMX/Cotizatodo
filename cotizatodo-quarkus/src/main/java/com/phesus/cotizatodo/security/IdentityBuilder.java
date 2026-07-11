package com.phesus.cotizatodo.security;

import com.phesus.cotizatodo.entity.User;
import com.phesus.cotizatodo.entity.UserRole;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;

final class IdentityBuilder {

    static final String ATTR_DISPLAY_NAME = "displayName";

    private IdentityBuilder() {
    }

    static SecurityIdentity from(User user) {
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal(user.getUsername()))
                .addAttribute(ATTR_DISPLAY_NAME, user.getFullname() != null ? user.getFullname() : user.getUsername());
        for (UserRole role : user.getUserRole()) {
            builder.addRole(role.getRole());
        }
        return builder.build();
    }
}
