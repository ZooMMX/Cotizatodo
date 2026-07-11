package com.phesus.cotizatodo.i18n;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.ext.Provider;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Resuelve el idioma de la petición replicando el LocaleChangeInterceptor +
 * SessionLocaleResolver de Spring: parámetro ?lang= > cookie > Accept-Language.
 * Cuando llega ?lang= se persiste en una cookie para las siguientes peticiones.
 */
@Provider
public class LocaleFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String COOKIE_NAME = "lang";
    private static final Set<String> SUPPORTED = Set.of("en", "es");
    private static final String CHANGED_PROP = LocaleFilter.class.getName() + ".changed";

    @Inject
    CurrentLocale currentLocale;

    @Override
    public void filter(ContainerRequestContext ctx) {
        String lang = ctx.getUriInfo().getQueryParameters().getFirst("lang");
        if (lang != null && SUPPORTED.contains(lang)) {
            ctx.setProperty(CHANGED_PROP, lang);
            currentLocale.setLang(lang);
            return;
        }

        Cookie cookie = ctx.getCookies().get(COOKIE_NAME);
        if (cookie != null && SUPPORTED.contains(cookie.getValue())) {
            currentLocale.setLang(cookie.getValue());
            return;
        }

        List<Locale> acceptable = ctx.getAcceptableLanguages();
        for (Locale locale : acceptable) {
            if (SUPPORTED.contains(locale.getLanguage())) {
                currentLocale.setLang(locale.getLanguage());
                return;
            }
        }

        currentLocale.setLang(MessageCatalog.DEFAULT_LANG);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object changed = requestContext.getProperty(CHANGED_PROP);
        if (changed != null) {
            responseContext.getHeaders().add("Set-Cookie",
                    COOKIE_NAME + "=" + changed + ";Path=/;Max-Age=31536000;SameSite=Lax");
        }
    }
}
