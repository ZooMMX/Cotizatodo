package com.phesus.cotizatodo.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phesus.cotizatodo.entity.Quote;
import com.phesus.cotizatodo.entity.QuoteRepository;
import com.phesus.cotizatodo.entity.User;
import com.phesus.cotizatodo.entity.UserRepository;
import com.phesus.cotizatodo.report.ReportService;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JsonQueryExecuterFactory;
import org.jboss.resteasy.reactive.RestQuery;

import javax.imageio.ImageIO;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Port del QuoteController de Spring MVC.
 */
@Path("/")
public class QuoteResource {

    private static final String DEFAULT_TAX = "Impuestos (IVA 16%)";

    @Inject QuoteRepository quotes;
    @Inject UserRepository users;
    @Inject ReportService reports;
    @Inject QuoteJsonMapper jsonMapper;
    @Inject ObjectMapper om;
    @Inject SecurityIdentity identity;

    @Inject @Location("quotes") Template quotesTemplate;
    @Inject Template quote_new;
    @Inject Template print_menu;

    @GET
    @Path("quotes")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance quotesPage(@RestQuery String success) {
        return quotesTemplate.data("success", success != null);
    }

    @GET
    @Path("quotes/new")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance newQuote() {
        return quote_new
                .data("quote", new Quote())
                .data("rows", List.of())
                .data("taxText", DEFAULT_TAX)
                .data("isNew", true);
    }

    @GET
    @Path("quotes/print")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance printMenu() {
        return print_menu.instance();
    }

    /**
     * Genera el PDF de la cotización. Público, igual que en la app legada
     * (imprimir no requiere cuenta).
     */
    @POST
    @Path("quotes/print/pdf")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/pdf")
    public Response printPdf(@BeanParam QuoteForm form) throws JRException, IOException {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(true);

        Quote quote = form.toQuote();

        Map<String, Object> params = new HashMap<>();
        params.put(JsonQueryExecuterFactory.JSON_INPUT_STREAM, jsonMapper.toJsonStream(nf, quote, form));
        params.put("SUBREPORT_JSON_INPUT_STREAM", jsonMapper.toJsonStream(nf, quote, form));

        if (form.hasLogo()) {
            try (InputStream is = java.nio.file.Files.newInputStream(form.logo.uploadedFile())) {
                params.put("logo", ImageIO.read(is));
            }
        }

        params.put("targetName", quote.getTargetName());
        params.put("targetPhone", quote.getTargetPhone());
        params.put("targetEmail", quote.getTargetEmail());
        params.put("targetAddress", quote.getTargetAddress());
        params.put("targetCompany", quote.getTargetCompany());
        params.put("targetPosition", quote.getTargetPosition());
        params.put("sourceName", quote.getSourceName());
        params.put("sourcePhone", quote.getSourcePhone());
        params.put("sourceEmail", quote.getSourceEmail());
        params.put("sourceAddress", quote.getSourceAddress());
        params.put("sourceCompany", quote.getSourceCompany());
        params.put("sourcePosition", quote.getSourcePosition());
        params.put("title", quote.getTitle());
        params.put("subtotal", quote.getSubtotal());
        params.put("taxes", quote.getTaxes());
        params.put("taxesDescription", quote.getTaxesDescription());
        params.put("total", quote.getTotal());
        params.put("folio", quote.getFolio());
        params.put("date", quote.getDate());
        params.put("termsAndConditions", quote.getTermsAndConditions());

        byte[] pdf = reports.renderPdf(form.layout, params);
        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=cotizacion.pdf")
                .build();
    }

    @POST
    @Path("quotes/save")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional
    public Response save(@BeanParam QuoteForm form) throws IOException, SQLException {
        String username = identity.getPrincipal().getName();

        Quote quote;
        Long id = form.quoteId();
        if (id != null) {
            quote = quotes.findById(id);
            if (quote == null)
                throw new NotFoundException();
            // Solo el dueño puede modificar su cotización
            if (quote.getUsername() == null || !quote.getUsername().equals(username))
                throw new ForbiddenException();
        } else {
            quote = new Quote();
        }

        form.applyTo(quote);

        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);
        quote.setItemsJson(jsonMapper.toJsonString(nf, quote, form));

        User user = users.findByUsername(username);
        quote.setUser(user);

        if (form.hasLogo()) {
            byte[] bytes = java.nio.file.Files.readAllBytes(form.logo.uploadedFile());
            quote.setLogoBytes(new javax.sql.rowset.serial.SerialBlob(bytes));
        }

        quotes.persist(quote);

        return Response.seeOther(URI.create("/quotes?success")).build();
    }

    @GET
    @Path("quote/{quoteId}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance viewQuote(@PathParam("quoteId") Long quoteId) throws IOException {
        Quote quote = requireOwnQuote(quoteId);

        List<Map<String, String>> details = List.of();
        if (quote.getItemsJson() != null && !quote.getItemsJson().isBlank()) {
            JsonNode nodes = om.readTree(quote.getItemsJson());
            JsonNode detailsNode = nodes.path("quote").path("details");
            details = om.convertValue(detailsNode,
                    om.getTypeFactory().constructCollectionType(List.class, Map.class));
        }

        return quote_new
                .data("quote", quote)
                .data("rows", details)
                .data("taxText", quote.getTaxesDescription())
                .data("isNew", false);
    }

    @GET
    @Path("quotes/viewLogo/{quoteId}")
    public Response viewLogo(@PathParam("quoteId") Long quoteId) throws IOException, SQLException {
        Quote quote = requireOwnQuote(quoteId);

        Blob blob = quote.getLogoBytes();
        if (blob == null || blob.length() == 0) {
            BufferedImage bi = new BufferedImage(1, 1, Transparency.TRANSLUCENT);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(bi, "PNG", out);
            return Response.ok(out.toByteArray()).type("image/png").build();
        }

        byte[] bytes = blob.getBinaryStream().readAllBytes();
        return Response.ok(bytes).type(MediaType.APPLICATION_OCTET_STREAM).build();
    }

    /**
     * Endpoint del DataTable de la lista de cotizaciones. También ejecuta la
     * acción grupal softdelete, validando que cada cotización pertenezca al
     * usuario activo (la app legada no lo validaba).
     */
    @GET
    @Path("quotes/JSON")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Map<String, Object> quotesJson(@RestQuery Integer length,
                                          @RestQuery Integer start,
                                          @RestQuery Integer draw,
                                          @RestQuery String customActionType,
                                          @RestQuery String customActionName,
                                          @RestQuery("id[]") List<Long> ids) {
        String username = identity.getPrincipal().getName();

        if ("group_action".equals(customActionType)
                && "softdelete".equals(customActionName)
                && ids != null) {
            disableQuotes(ids, username);
        }

        int first = start != null ? start : 0;
        int max = (length != null && length > 0) ? length : Integer.MAX_VALUE;

        var query = quotes.find("enabled = true and username = ?1", Sort.descending("id"), username);
        long filtered = query.count();
        List<Quote> page = query.range(first, first + max - 1).list();

        Map<String, Object> resp = new HashMap<>();
        resp.put("data", page);
        resp.put("draw", draw);
        resp.put("recordsTotal", quotes.countUserQuotes(username));
        resp.put("recordsFiltered", filtered);
        return resp;
    }

    /** Soft-delete: solo desactiva cotizaciones del usuario activo. */
    private void disableQuotes(List<Long> ids, String username) {
        for (Long id : ids) {
            Quote quote = quotes.findById(id);
            if (quote != null && username.equals(quote.getUsername()))
                quote.setEnabled(false);
        }
    }

    private Quote requireOwnQuote(Long quoteId) {
        Quote quote = quotes.findById(quoteId);
        if (quote == null)
            throw new NotFoundException();
        String username = identity.getPrincipal().getName();
        if (quote.getUsername() == null || !quote.getUsername().equals(username))
            throw new ForbiddenException();
        return quote;
    }
}
