package com.phesus.cotizatodo;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.bytecode.ByteArray;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.ConfigurableJasperReportsView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Proyecto cotizatodo
 * User: octavioruizcastillo
 * Date: 03/02/15
 * Time: 15:54
 */
@Controller
public class QuoteController {

    @Autowired private ApplicationContext appContext;
    @Autowired private QuoteRepository repo;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager em ;
    private final String DEFAULT_TAX = "Impuestos (IVA 16%)";

    @RequestMapping("/quotes")
    public String quotes(Model model) {

        return "quotes";
    }

    @RequestMapping("/quotes/new")
    public String newQuote(Model model)
    {
        model.addAttribute("quote", new Quote());
        model.addAttribute("taxText", DEFAULT_TAX);
        return "quote_new";
    }

    @RequestMapping(value = "/quotes/print", method = RequestMethod.POST)
    public @ResponseBody String print(@ModelAttribute Quote quote, Model model)
    {
        return "ok";
    }

    @RequestMapping(value = "/quotes/print")
    public String printMenu(Model model)
    {
        return "print_menu";
    }

    /**
     * This method outputs the PDF Quote / Método que genera el PDF con la cotización
     * @param quote
     * @param layout
     * @param rowsDescription
     * @param quantity
     * @param unitPrice
     * @param rowTotal
     * @param logoFile
     * @param response
     * @return
     * @throws IOException
     * @throws JRException
     */
    @RequestMapping(value = "/quotes/print/pdf", method = RequestMethod.POST)
    public ModelAndView fullReport(
            @ModelAttribute Quote quote,
            @RequestParam String layout,
            @RequestParam(value = "description[]", required = false) String[] rowsDescription,
            @RequestParam(value = "quantity[]", required = false) BigDecimal[] quantity,
            @RequestParam(value = "buyPrice[]", required = false) BigDecimal[] buyPrice,
            @RequestParam(value = "markup[]", required = false) BigDecimal[] markup,
            @RequestParam(value = "profit[]", required = false) BigDecimal[] profit,
            @RequestParam(value = "unitPrice[]", required = false) BigDecimal[] unitPrice,
            @RequestParam(value = "unitSellPrice[]", required = false) BigDecimal[] unitSellPrice,
            @RequestParam(value = "profitTotal[]", required = false) BigDecimal[] profitTotal,
            @RequestParam(value = "rowTotal[]", required = false) BigDecimal[] rowTotal,
            @RequestParam(value= "logo", required = false) MultipartFile logoFile,
            HttpServletResponse response) throws IOException, JRException {

        ObjectMapper om = new ObjectMapper();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Map<String, Object> map = new HashMap<String, Object>() {{
            put("quote", quote);
        }};
        om.writer().writeValue(out, map);
        //System.out.println(om.writerWithDefaultPrettyPrinter().writeValueAsString(map));

        final JasperReportsPdfView view = new JasperReportsPdfView();
        view.setReportDataKey("users");
        view.setUrl( getLayout(layout) );
        view.setApplicationContext(appContext);
        view.setReportDataKey("datasource");

        final Map<String, Object> params = new HashMap<>();
        //JRDataSource ds = new JsonDataSource(new File("demoData.json"), "quote");
        JRDataSource ds = new JsonDataSource(new ByteArrayInputStream(out.toByteArray()), "quote");

        params.put("datasource", ds);

        params.put("JSON_INPUT_STREAM", fields2Json(rowsDescription, quantity, buyPrice, markup, profit, unitPrice, unitSellPrice, profitTotal, rowTotal)); //new FileInputStream("demoData.json"));
        if(logoFile != null)
            params.put("logo", ImageIO.read(logoFile.getInputStream()));
        //Funcionando con demo data: params.put("JSON_INPUT_STREAM", new ByteArrayInputStream(out.toByteArray()));

        return new ModelAndView(view, params);

    }

    @PreAuthorize("isAuthenticated() and hasPermission(#quoteId, 'quote')")
    @RequestMapping("/quotes/viewLogo/{quoteId}")
    public String viewQuoteLogo(
            @PathVariable("quoteId") Long quoteId,
            HttpServletResponse response
        ) {
        Quote q = repo.findOne(quoteId);

        response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
        try {
            /* If a image has been store, then copy his bytes to output */
            if(q.getLogoBytes() == null || q.getLogoBytes().length() == 0) {
                BufferedImage bi = new BufferedImage(1,1, Transparency.TRANSLUCENT);
                OutputStream out = response.getOutputStream();
                ImageIO.write(bi, "PNG", out);

            /* Already stored image */
            } else {
                Integer leng = (int) q.getLogoBytes().length();
                response.setContentLength(leng);
                OutputStream out = response.getOutputStream();

                IOUtils.copy(q.getLogoBytes().getBinaryStream(), out);
                out.flush();
                out.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    /**
     * 1. Por defecto genera un mapa de mapas de listas de un mapa -- map[map[list[map]]] -- que contiene la información de la tabla de la cotización.
     * 2. Lo convierte a JSON
     * 3. Lo convierte a un ByteArrayOutputStream
     * 4. Lo convierte en un ByteArrayInputStream
     *
     * @param description
     * @param quantity
     * @param unitPrice
     * @param rowTotal
     * @return
     * @throws IOException
     */
    public InputStream fields2Json(String[] description, BigDecimal[] quantity, BigDecimal[] buyPrice, BigDecimal[] markup, BigDecimal[] profit, BigDecimal[] unitPrice, BigDecimal[] unitSellPrice, BigDecimal[] profitTotal, BigDecimal[] rowTotal) throws IOException {
        ObjectMapper om = new ObjectMapper();

        ArrayList<Map<String, String>> lista = mapFields(description, quantity, buyPrice, markup, profit, unitPrice, unitSellPrice, profitTotal, rowTotal);
        HashMap<String, ArrayList<Map<String, String>>> outerMap = new HashMap<>();
        outerMap.put("details", lista);

        HashMap outerOuterMap = new HashMap<>();
        outerOuterMap.put("quote", outerMap);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        om.writeValue( out, outerOuterMap );
        ByteArrayInputStream bais = new ByteArrayInputStream(out.toByteArray());

        return bais;
    }

    public ArrayList<Map<String, String>> mapFields(String[] description, BigDecimal[] quantity, BigDecimal[] buyPrice, BigDecimal[] markup, BigDecimal[] profit, BigDecimal[] unitPrice, BigDecimal[] unitSellPrice, BigDecimal[] profitTotal, BigDecimal[] rowTotal) {
        ArrayList<Map<String, String>> lista = new ArrayList<>();
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);

        for (int i = 0; i < description.length; i++) {
            HashMap<String, String> map = new HashMap<>();
            //Common fields
            map.put("concept", description[i]);
            map.put("quantity", nf.format( quantity[i] ));
            map.put("unitPrice", nf.format( unitPrice[i]) );
            map.put("total", nf.format( rowTotal[i]) );

            //Advance fields, use if exists, otherwise fill with zeros
            if(i < buyPrice.length) {
                map.put("buyPrice", nf.format(buyPrice[i]));
                map.put("markup", nf.format(markup[i]));
                map.put("profit", nf.format(profit[i]));
                map.put("unitSellPrice", nf.format(unitSellPrice[i]));
                map.put("profitTotal", nf.format(profitTotal[i]));
            } else {
                map.put("buyPrice", map.get("unitPrice") );
                map.put("markup", nf.format( BigDecimal.ZERO ));
                map.put("profit", nf.format( BigDecimal.ZERO ));
                map.put("unitSellPrice", nf.format( BigDecimal.ZERO )); //Uses the same value
                map.put("profitTotal", nf.format( BigDecimal.ZERO ));
            }

            lista.add(map);
        }
        return lista;
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#quote.id, 'quote')")
    @RequestMapping(value = "/quotes/save", method = RequestMethod.POST)
    public ModelAndView fullReport(
            @ModelAttribute Quote quote,
            @RequestParam(value= "logo", required = false) MultipartFile logoFile,
            @RequestParam(value = "description[]", required = false) String[] rowsDescription,
            @RequestParam(value = "quantity[]", required = false) BigDecimal[] quantity,
            @RequestParam(value = "buyPrice[]", required = false) BigDecimal[] buyPrice,
            @RequestParam(value = "markup[]", required = false) BigDecimal[] markup,
            @RequestParam(value = "profit[]", required = false) BigDecimal[] profit,
            @RequestParam(value = "unitPrice[]", required = false) BigDecimal[] unitPrice,
            @RequestParam(value = "unitSellPrice[]", required = false) BigDecimal[] unitSellPrice,
            @RequestParam(value = "profitTotal[]", required = false) BigDecimal[] profitTotal,
            @RequestParam(value = "rowTotal[]", required = false) BigDecimal[] rowTotal,
            HttpServletResponse response,
            Principal principal) throws IOException, SQLException {

        /* Transform the logo from Bytes to Blob */
        Blob blob = null;
        if(logoFile != null)
            blob = new javax.sql.rowset.serial.SerialBlob(logoFile.getBytes());

        /* Transform cells into JSON */
        InputStream inputStream = fields2Json(rowsDescription, quantity, buyPrice, markup, profit, unitPrice, unitSellPrice, profitTotal, rowTotal);
        String itemsJson = convertStreamToString(inputStream);
        inputStream.close();

        /* Gather information about user */
        MediUser activeUser = (MediUser) ((Authentication) principal).getPrincipal();
        User user = userRepository.findOne(activeUser.getUsername());

        /* Save/Update quote */
        quote.setUser(user);
        quote.setItemsJson(itemsJson);
        // Change logo only if it changed. If logo==null I assume it didn't change
        if(blob != null) quote.setLogoBytes(blob);
        repo.save(quote);

        return new ModelAndView("redirect:/quotes?success");
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @PreAuthorize("isAuthenticated() and hasPermission(#quoteId, 'quote')")
    @RequestMapping(value = "/quote/{quoteId}")
    public String viewQuote(
            @PathVariable Long quoteId,
            Model model,
            Principal principal
    ) throws IOException {
        Quote quote = repo.findOne(quoteId);

        ObjectMapper om = new ObjectMapper();
        //Quote -> details -> [] -> total, quantity, price, concept
        HashMap<String, HashMap<String, ArrayList<Map<String, String>>>> tableContent = om.readValue(quote.getItemsJson(), new TypeReference<HashMap<String, HashMap<String, ArrayList<Map<String, String>>>>>() {});

        model.addAttribute("quote", quote);
        model.addAttribute("rows", tableContent.get("quote").get("details"));
        model.addAttribute("taxText", quote.getTaxesDescription());
        return "quote_new";
    }

    @RequestMapping(value = "/quotes/JSON")
    public @ResponseBody HashMap<String, Object> quotesJSON(
        @RequestParam Integer length,
        @RequestParam Integer start,
        @RequestParam Integer draw,
        /* customActionType, customActionName e id son campos de la implementación de la acción Archivar/Ocultar/Softdelete */
        @RequestParam(required = false) String customActionType,
        @RequestParam(required = false) String customActionName,
        @RequestParam(value = "id[]", required = false) Long[] ids,
        Model model,
        Principal principal) {
            // Setup
            MediUser activeUser = (MediUser) ((Authentication) principal).getPrincipal();
            HashMap resp = new HashMap<String, Object>();
            Integer pageNo = start / length;

            /* Ejecución de acciones */
            if(customActionType != null && customActionType.equals("group_action"))
                // ** ¿Borrar? **
                if(customActionName != null && customActionName.equals("softdelete") && ids != null) disableQuotes(ids);
            /* Terminan acciones */

            /* Comienza filtros mediante criteria builder de JPA */
            CriteriaBuilder qb = em.getCriteriaBuilder();
            CriteriaQuery<Quote> quoteQuery = qb.createQuery(Quote.class);
            Root<Quote> p = quoteQuery.from(Quote.class);
            //Establezco una condición notNull únicamente para inicializar las condiciones
            Predicate condiciones = qb.isNotNull(p.get(Quote_.id));

            // ** Filtro de cotizaciones activas **
            if(true) {
                Predicate condicion = qb.equal(p.get(Quote_.enabled), true);
                condiciones = qb.and(condiciones, condicion);
            }

            // ** Filtro de usuario activo **
            Predicate condicion = qb.equal(p.get(Quote_.username), activeUser.getUsername());
            condiciones = qb.and(condiciones, condicion);

            quoteQuery.where(condiciones);
            quoteQuery.orderBy(qb.desc(p.get(Quote_.id)));
            /* **** Terminan filtros *** */

            /* ** Paginación y ejecución de consulta ** */
            TypedQuery<Quote> piezasTypedQuery = em.createQuery( quoteQuery );
            piezasTypedQuery.setFirstResult(start);
            piezasTypedQuery.setMaxResults(length);

            List<Quote> piezas = piezasTypedQuery.getResultList(); //Ejecución de la consulta maestra
            CriteriaQuery countQuery = quoteQuery;
            countQuery.select( qb.count( p.get(Quote_.id) ) );
            Long piezasFiltradas = (Long) em.createQuery( countQuery ).getSingleResult();   //Ejecución de la consulta conteo de elementos filtrados

            /* Renderizar resultado */
            model.addAttribute("name", "¿nombre?");
            resp.put("data", piezas);
            resp.put("draw", draw);
            resp.put("recordsTotal", repo.countUserQuotes( activeUser.getUsername() ));
            resp.put("recordsFiltered", piezasFiltradas);
            return resp;
    }


    private String getLayout(String layout) {

        switch(layout) {
            case "first_magenta": return "classpath:Invoice_Table_Based_layout1.jrxml";
            case "first_blue": return "classpath:Invoice_Table_Based_layout1b.jrxml";
            case "first_green": return "classpath:Invoice_Table_Based_layout1c.jrxml";
        }

        return "classpath:Invoice_Table_Based_layout1.jrxml";
    }

    /**
     * This method is used by quotesJSON method (/quotes/JSON/).
     * Soft-delete quotes disabling them.
     * @param id
     */
    @Transactional
    private void disableQuotes(Long[] id) {
       for (Long aLong : id) {
           Quote p = repo.findOne(aLong);
           p.setEnabled(false);
           repo.save(p);
       }
    }
}
