package com.phesus.cotizatodo.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.phesus.cotizatodo.entity.Quote;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convierte la cotización + filas del formulario al JSON que consumen las
 * plantillas Jasper (raíz "quote" con arreglo "details"). Port de
 * fields2Json/mapFields/sanitizeQuoteRows del QuoteController legado.
 */
@ApplicationScoped
public class QuoteJsonMapper {

    @Inject
    ObjectMapper om;

    public InputStream toJsonStream(NumberFormat nf, Quote quote, QuoteForm form) {
        try {
            ObjectNode rootNode = om.createObjectNode();
            ObjectNode quoteNode = om.valueToTree(quote);
            rootNode.set("quote", quoteNode);
            JsonNode detailsNode = om.valueToTree(mapRows(nf, form));
            quoteNode.set("details", detailsNode);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            om.writeValue(out, rootNode);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String toJsonString(NumberFormat nf, Quote quote, QuoteForm form) {
        try (InputStream is = toJsonStream(nf, quote, form)) {
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Map<String, String>> mapRows(NumberFormat nf, QuoteForm form) {
        List<Map<String, String>> lista = new ArrayList<>();
        int rows = form.description != null ? form.description.size() : 0;

        for (int i = 0; i < rows; i++) {
            Map<String, String> map = new HashMap<>();
            map.put("concept", form.description.get(i));
            map.put("quantity", numberAt(nf, form.quantity, i));
            map.put("total", numberAt(nf, form.rowTotal, i));
            map.put("buyPrice", numberAt(nf, form.buyPrice, i));
            map.put("markup", numberAt(nf, form.markup, i));
            map.put("profit", numberAt(nf, form.profit, i));
            map.put("unitSellPrice", numberAt(nf, form.unitSellPrice, i));
            map.put("profitTotal", numberAt(nf, form.profitTotal, i));

            // En modo avanzado unitPrice = unitSellPrice; en modo simple viene unitPrice
            String unitSell = rawAt(form.unitSellPrice, i);
            String unit = rawAt(form.unitPrice, i);
            if (unitSell != null)
                map.put("unitPrice", format(nf, unitSell));
            else if (unit != null)
                map.put("unitPrice", format(nf, unit));
            else
                map.put("unitPrice", nf.format(BigDecimal.ZERO));

            sanitizeRow(nf, map);
            lista.add(map);
        }
        return lista;
    }

    private String rawAt(List<String> values, int i) {
        if (values == null || i >= values.size())
            return null;
        String value = values.get(i);
        return (value == null || value.isBlank()) ? null : value;
    }

    private String numberAt(NumberFormat nf, List<String> values, int i) {
        String raw = rawAt(values, i);
        return raw != null ? format(nf, raw) : nf.format(BigDecimal.ZERO);
    }

    private String format(NumberFormat nf, String raw) {
        try {
            return nf.format(new BigDecimal(raw.trim()));
        } catch (NumberFormatException e) {
            return nf.format(BigDecimal.ZERO);
        }
    }

    /** Garantiza que ninguna celda del PDF llegue nula. */
    private void sanitizeRow(NumberFormat nf, Map<String, String> map) {
        map.putIfAbsent("concept", "");
        for (String key : new String[]{"quantity", "total", "buyPrice", "markup", "profit",
                "unitSellPrice", "profitTotal", "unitPrice"}) {
            if (map.get(key) == null)
                map.put(key, nf.format(BigDecimal.ZERO));
        }
    }
}
