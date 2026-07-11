package com.phesus.cotizatodo.i18n;

import com.phesus.cotizatodo.security.SecurityHelper;
import io.quarkus.arc.Arc;
import io.quarkus.qute.TemplateExtension;

/**
 * Espacios de nombres disponibles en las plantillas Qute:
 *
 *   {msg:t('clave')}      → mensaje i18n en el idioma de la petición
 *   {msg:lang}            → idioma efectivo (en|es)
 *   {sec:authenticated}   → true si hay sesión iniciada
 *   {sec:displayName}     → nombre a mostrar del usuario autenticado
 */
public class TemplateExtensions {

    @TemplateExtension(namespace = "msg")
    static String t(String key) {
        CurrentLocale locale = Arc.container().instance(CurrentLocale.class).get();
        MessageCatalog catalog = Arc.container().instance(MessageCatalog.class).get();
        return catalog.get(locale.getLang(), key);
    }

    @TemplateExtension(namespace = "msg")
    static String lang() {
        return Arc.container().instance(CurrentLocale.class).get().getLang();
    }

    /** Año actual del servidor, para el copyright del footer: {app:year}. */
    @TemplateExtension(namespace = "app")
    static int year() {
        return java.time.Year.now().getValue();
    }

    @TemplateExtension(namespace = "sec")
    static boolean authenticated() {
        return SecurityHelper.isAuthenticated();
    }

    @TemplateExtension(namespace = "sec")
    static String displayName() {
        return SecurityHelper.displayName();
    }
}
