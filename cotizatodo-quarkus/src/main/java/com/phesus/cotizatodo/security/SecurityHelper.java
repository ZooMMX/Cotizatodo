package com.phesus.cotizatodo.security;

import io.quarkus.arc.Arc;
import io.quarkus.security.identity.SecurityIdentity;

public final class SecurityHelper {

    private SecurityHelper() {
    }

    private static SecurityIdentity identity() {
        return Arc.container().instance(SecurityIdentity.class).get();
    }

    public static boolean isAuthenticated() {
        SecurityIdentity identity = identity();
        return identity != null && !identity.isAnonymous();
    }

    public static String displayName() {
        SecurityIdentity identity = identity();
        if (identity == null || identity.isAnonymous())
            return "";
        String displayName = identity.getAttribute(IdentityBuilder.ATTR_DISPLAY_NAME);
        return displayName != null ? displayName : identity.getPrincipal().getName();
    }
}
