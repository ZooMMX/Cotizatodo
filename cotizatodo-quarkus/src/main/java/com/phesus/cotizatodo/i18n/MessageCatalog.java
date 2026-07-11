package com.phesus.cotizatodo.i18n;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Catálogo de mensajes i18n. Reutiliza los bundles ISO-8859-1
 * i18n/messages_{es,en}.properties de la app legada.
 */
@ApplicationScoped
@Unremovable
public class MessageCatalog {

    public static final String DEFAULT_LANG = "en";

    private final Map<String, Properties> bundles = new ConcurrentHashMap<>();

    public String get(String lang, String key) {
        String value = bundle(lang).getProperty(key);
        if (value == null && !DEFAULT_LANG.equals(lang))
            value = bundle(DEFAULT_LANG).getProperty(key);
        return value != null ? value : key;
    }

    private Properties bundle(String lang) {
        return bundles.computeIfAbsent(lang, l -> {
            Properties props = new Properties();
            try (InputStream is = getClass().getResourceAsStream("/i18n/messages_" + l + ".properties")) {
                if (is != null)
                    props.load(is);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return props;
        });
    }
}
