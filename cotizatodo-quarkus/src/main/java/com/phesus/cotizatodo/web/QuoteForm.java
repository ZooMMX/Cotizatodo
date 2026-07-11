package com.phesus.cotizatodo.web;

import com.phesus.cotizatodo.entity.Quote;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.List;

/**
 * Binding del formulario de cotización (quote_new.html). Reemplaza el
 * @ModelAttribute Quote + @RequestParam de Spring MVC.
 */
public class QuoteForm {

    @RestForm public String id;
    @RestForm public String layout;

    @RestForm public String title;
    @RestForm public String folio;
    @RestForm public String date;

    @RestForm public String sourceName;
    @RestForm public String sourcePosition;
    @RestForm public String sourceCompany;
    @RestForm public String sourceAddress;
    @RestForm public String sourcePhone;
    @RestForm public String sourceEmail;

    @RestForm public String targetName;
    @RestForm public String targetPosition;
    @RestForm public String targetCompany;
    @RestForm public String targetPhone;

    @RestForm public String termsAndConditions;
    @RestForm public String taxesDescription;
    @RestForm public String taxes;
    @RestForm public String subtotal;
    @RestForm public String total;

    @RestForm("description[]") public List<String> description;
    @RestForm("quantity[]") public List<String> quantity;
    @RestForm("buyPrice[]") public List<String> buyPrice;
    @RestForm("markup[]") public List<String> markup;
    @RestForm("profit[]") public List<String> profit;
    @RestForm("unitPrice[]") public List<String> unitPrice;
    @RestForm("unitSellPrice[]") public List<String> unitSellPrice;
    @RestForm("profitTotal[]") public List<String> profitTotal;
    @RestForm("rowTotal[]") public List<String> rowTotal;

    @RestForm public FileUpload logo;

    public Long quoteId() {
        if (id == null || id.isBlank())
            return null;
        try {
            return Long.valueOf(id.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean hasLogo() {
        return logo != null && logo.size() > 0;
    }

    /** Vuelca los campos del formulario en la entidad (sin tocar user/logo/enabled/created). */
    public void applyTo(Quote quote) {
        quote.setTitle(title);
        quote.setFolio(folio);
        quote.setDate(date);
        quote.setSourceName(sourceName);
        quote.setSourcePosition(sourcePosition);
        quote.setSourceCompany(sourceCompany);
        quote.setSourceAddress(sourceAddress);
        quote.setSourcePhone(sourcePhone);
        quote.setSourceEmail(sourceEmail);
        quote.setTargetName(targetName);
        quote.setTargetPosition(targetPosition);
        quote.setTargetCompany(targetCompany);
        quote.setTargetPhone(targetPhone);
        quote.setTermsAndConditions(termsAndConditions);
        quote.setTaxesDescription(taxesDescription != null ? taxesDescription.replace("\n", "").trim() : "");
        quote.setTaxes(taxes != null ? taxes : "");
        quote.setSubtotal(subtotal != null ? subtotal : "");
        quote.setTotal(total != null ? total : "");
    }

    /** Quote transitorio para imprimir sin guardar. */
    public Quote toQuote() {
        Quote quote = new Quote();
        applyTo(quote);
        return quote;
    }
}
