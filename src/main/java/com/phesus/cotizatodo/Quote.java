package com.phesus.cotizatodo;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Proyecto Cotizatodo
 * User: octavioruizcastillo
 * Date: 30/01/15
 * Time: 3:34
 */
@Entity
public class Quote {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    @Column private String title = "COTIZACIÓN";

    @Column private String folio = "A876";

    @Column private String sourceName;
    @Column private String sourcePosition;
    @Column private String sourceCompany = "Tu empresa";
    @Column private String sourceAddress = "1001 Av. Juarez\nCol. Centro\nPuebla, Pue.";
    @Column private String sourcePhone = "(222) 984 112" ;
    @Column private String sourceEmail = "ventas@cotizadora.com";
    @Column private String targetName = "Horacio Orozco";

    @Column private String targetPosition = "CEO";
    @Column private String targetCompany = "Orozco Co.";
    @Column private String targetAddress;
    @Column private String targetPhone   = "(55) 12 23 34 45 56";
    @Column private String targetEmail;
    @Column private String termsAndConditions = "Páguese antes de 60 días.";

    @Column private String taxesDescription = "IVA (16%)";
    @Column private String taxes            = "";
    @Column private String subtotal         = "";
    @Column private String total            = "";

    @Column private String itemsJson;

    @Column private String date = "1 de Febrero de 2015";
    @Column private Date created;
    @Column private Date updated;

    @JsonIgnore
    @ManyToOne
    User user;

    @JsonIgnore
    @Column(name = "user_username", updatable = false, insertable = false)
    String username;

    @PrePersist
    protected void onCreate() {
      created = new Date();
       updated = new Date();

    }

    @PreUpdate
    protected void onUpdate() {
      updated = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUsername() { return username; }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourcePosition() {
        return sourcePosition;
    }

    public void setSourcePosition(String sourcePosition) {
        this.sourcePosition = sourcePosition;
    }

    public String getSourceCompany() {
        return sourceCompany;
    }

    public void setSourceCompany(String sourceCompany) {
        this.sourceCompany = sourceCompany;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getSourcePhone() {
        return sourcePhone;
    }

    public void setSourcePhone(String sourcePhone) {
        this.sourcePhone = sourcePhone;
    }

    public String getSourceEmail() {
        return sourceEmail;
    }

    public void setSourceEmail(String sourceEmail) {
        this.sourceEmail = sourceEmail;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(String targetPosition) {
        this.targetPosition = targetPosition;
    }

    public String getTargetCompany() {
        return targetCompany;
    }

    public void setTargetCompany(String targetCompany) {
        this.targetCompany = targetCompany;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }

    public String getTargetPhone() {
        return targetPhone;
    }

    public void setTargetPhone(String targetPhone) {
        this.targetPhone = targetPhone;
    }

    public String getTargetEmail() {
        return targetEmail;
    }

    public void setTargetEmail(String targetEmail) {
        this.targetEmail = targetEmail;
    }

    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(String termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getItemsJson() {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson) {
        this.itemsJson = itemsJson;
    }

    public String getUpdatedAsString() {
        DateFormat sdf = SimpleDateFormat.getDateInstance();
        Date updated = getUpdated();
        String fechaString;
        if(updated == null)
                return "01/01/2000";
            else
                return sdf.format(updated);

    }

    public String getHtmlCheckbox() {
       String ret = new String("<input type=\"checkbox\" name=\"id[]\" value=\"" + getId() + "\">");
        return ret;
    }

    public String getHtmlAction() {
        StringBuilder builder = new StringBuilder();
        builder.append(new String("<a href=\"/quote/"+ getId() +"\" class=\"btn btn-xs blue\"><i class=\"fa fa-search\"></i> Ver</a>"));
        return builder.toString();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaxesDescription() {
        return taxesDescription;
    }

    public void setTaxesDescription(String taxesDescription) {
        this.taxesDescription = taxesDescription;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTotal() {
        if(total != null && total.length() > 0) {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            return nf.format(new BigDecimal(total != null ? total : "0"));
        } else {
            return "0.00";
        }
    }

    public void setTotal(String total) {
        this.total = total ;
    }

    public String getSubtotal() {
        if(subtotal != null && subtotal.length() > 0) {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            return nf.format(new BigDecimal(subtotal));
        } else {
            return "0.00";
        }
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public String getTaxes() {
        if(taxes != null && taxes.length() > 0) {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            return nf.format(new BigDecimal(taxes));
        } else {
            return "0.00";
        }
    }

    public void setTaxes(String taxes) {
        this.taxes = taxes;
    }
}
