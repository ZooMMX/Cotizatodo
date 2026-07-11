package com.phesus.cotizatodo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Blob;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Cotización. Mapea la tabla `quote` del esquema legado.
 *
 * Los getters htmlCheckbox/htmlAction/updatedAsString se conservan porque el
 * DataTable de quotes.html los consume tal cual desde /quotes/JSON.
 */
@Entity
@Table(name = "quote")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column private String title;
    @Column private String folio;

    @JsonIgnore
    @Lob
    private Blob logoBytes;

    @Column private String sourceName;
    @Column private String sourceCompany;
    @Column private String sourceAddress;
    @Column private String sourcePhone;
    @Column private String sourceEmail;
    @Column private String sourcePosition;

    @Column private String targetName;
    @Column private String targetPosition;
    @Column private String targetCompany;
    @Column private String targetAddress = "";
    @Column private String targetPhone;
    @Column private String targetEmail = "";

    @Column private String termsAndConditions;
    @Column private String taxesDescription = "VAT (20%)";

    @Column private String taxes = "";
    @Column private String subtotal = "";
    @Column private String total = "";

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column
    private String itemsJson;

    @Column private String date;

    @Column private Date created = new Date();
    @Column private Date updated;
    @Column private Boolean enabled = true;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_username")
    private User user;

    @JsonIgnore
    @Column(name = "user_username", updatable = false, insertable = false)
    private String username;

    @PrePersist
    protected void onCreate() {
        created = new Date();
        updated = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }

    /* ---- Campos calculados que consume el DataTable ---- */

    public String getUpdatedAsString() {
        DateFormat sdf = SimpleDateFormat.getDateInstance();
        Date updated = getUpdated();
        if (updated == null)
            return "01/01/2000";
        else
            return sdf.format(updated);
    }

    public String getHtmlCheckbox() {
        return "<input type=\"checkbox\" name=\"id[]\" value=\"" + getId() + "\">";
    }

    public String getHtmlAction() {
        return "<a href=\"/quote/" + getId() + "\" class=\"btn btn-xs blue\"><i class=\"fa fa-search\"></i> View</a>";
    }

    /* ---- Getters/setters ---- */

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public Blob getLogoBytes() {
        return logoBytes;
    }

    public void setLogoBytes(Blob logoBytes) {
        this.logoBytes = logoBytes;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
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

    public String getSourcePosition() {
        return sourcePosition;
    }

    public void setSourcePosition(String sourcePosition) {
        this.sourcePosition = sourcePosition;
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

    public String getTaxesDescription() {
        return taxesDescription;
    }

    public void setTaxesDescription(String taxesDescription) {
        this.taxesDescription = taxesDescription;
    }

    /** Total formateado como moneda, igual que la app legada. */
    public String getTotal() {
        if (total != null && total.length() > 0) {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            return nf.format(new BigDecimal(total));
        } else {
            return "0.00";
        }
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getSubtotal() {
        if (subtotal != null && subtotal.length() > 0) {
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
        if (taxes != null && taxes.length() > 0) {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            return nf.format(new BigDecimal(taxes));
        } else {
            return "0.00";
        }
    }

    public void setTaxes(String taxes) {
        this.taxes = taxes;
    }

    public String getItemsJson() {
        return itemsJson;
    }

    public void setItemsJson(String itemsJson) {
        this.itemsJson = itemsJson;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
