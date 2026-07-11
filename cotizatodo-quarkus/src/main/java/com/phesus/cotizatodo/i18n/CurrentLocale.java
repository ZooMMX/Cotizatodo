package com.phesus.cotizatodo.i18n;

import jakarta.enterprise.context.RequestScoped;

/**
 * Idioma efectivo de la petición actual (en|es), resuelto por LocaleFilter.
 */
@RequestScoped
public class CurrentLocale {

    private String lang = MessageCatalog.DEFAULT_LANG;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
