package com.phesus.cotizatodo.report;

import jakarta.enterprise.context.ApplicationScoped;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compila (una sola vez) y llena las plantillas .jrxml legadas.
 * Reemplaza al JasperReportsPdfView de Spring.
 */
@ApplicationScoped
public class ReportService {

    private final Map<String, JasperReport> compiled = new ConcurrentHashMap<>();

    /** Mapea el nombre de layout del formulario a su plantilla, igual que la app legada. */
    public String resolveLayout(String layout) {
        return switch (layout != null ? layout : "") {
            case "first_magenta" -> "/Invoice_Table_Based_layout1.jrxml";
            case "first_blue" -> "/Invoice_Table_Based_layout1b.jrxml";
            case "first_green" -> "/Invoice_Table_Based_layout1c.jrxml";
            case "second" -> "/Invoice_layout2.jrxml";
            default -> "/Invoice_Table_Based_layout1.jrxml";
        };
    }

    public byte[] renderPdf(String layout, Map<String, Object> params) throws JRException {
        JasperReport report = compiled.computeIfAbsent(resolveLayout(layout), this::compile);
        JasperPrint print = JasperFillManager.fillReport(report, params);
        return JasperExportManager.exportReportToPdf(print);
    }

    private JasperReport compile(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null)
                throw new IllegalStateException("Plantilla de reporte no encontrada: " + resourcePath);
            return JasperCompileManager.compileReport(is);
        } catch (JRException e) {
            throw new IllegalStateException("No se pudo compilar el reporte " + resourcePath, e);
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }
}
