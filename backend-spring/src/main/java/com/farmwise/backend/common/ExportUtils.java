package com.farmwise.backend.common;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public final class ExportUtils {

    private ExportUtils() {
    }

    public static byte[] toCsvBytes(List<String> headers, List<List<String>> rows) {
        StringWriter writer = new StringWriter();
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(headers.toArray(String[]::new))
                .build();
        try (CSVPrinter printer = new CSVPrinter(writer, format)) {
            for (List<String> row : rows) {
                printer.printRecord(row);
            }
            printer.flush();
            return writer.toString().getBytes(StandardCharsetsSafe.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to generate CSV report", ex);
        }
    }

    public static byte[] toPdfBytes(
            String reportTitle,
            String subtitle,
            List<String> headers,
            List<List<String>> rows,
            String footerDisclaimer) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4.rotate(), 28, 28, 32, 32);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(14, 59, 43));
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(75, 85, 99));
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(17, 24, 39));

            Paragraph title = new Paragraph("FarmWise — " + reportTitle, titleFont);
            title.setSpacingAfter(4);
            doc.add(title);

            String generated = "Generated UTC: "
                    + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(OffsetDateTime.now(ZoneOffset.UTC));
            Paragraph sub = new Paragraph(subtitle + "  |  " + generated, subFont);
            sub.setSpacingAfter(14);
            doc.add(sub);

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);
            table.setHeaderRows(1);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
                cell.setBackgroundColor(new Color(20, 77, 57));
                cell.setPadding(6);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(cell);
            }

            boolean alt = false;
            for (List<String> row : rows) {
                Color bg = alt ? new Color(245, 247, 242) : Color.WHITE;
                for (int i = 0; i < headers.size(); i++) {
                    String val = i < row.size() && row.get(i) != null ? row.get(i) : "";
                    PdfPCell cell = new PdfPCell(new Phrase(val, cellFont));
                    cell.setBackgroundColor(bg);
                    cell.setPadding(5);
                    table.addCell(cell);
                }
                alt = !alt;
            }
            doc.add(table);

            if (footerDisclaimer != null && !footerDisclaimer.isBlank()) {
                Paragraph disclaimer = new Paragraph("\nNote: " + footerDisclaimer, subFont);
                doc.add(disclaimer);
            }

            doc.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate PDF report", ex);
        }
    }

    private static final class StandardCharsetsSafe {
        private static final java.nio.charset.Charset UTF_8 = java.nio.charset.StandardCharsets.UTF_8;
    }
}
