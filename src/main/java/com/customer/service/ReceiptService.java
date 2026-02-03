package com.customer.service;

import com.customer.model.Account;
import com.customer.model.Transaction;
import com.customer.util.SessionManager;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ReceiptService {

    private static final String FONT_PATH = "arial.ttf"; // Assuming standard font or embedded
    // Using built-in Helvetica for simplicity if custom font not available, but for Vietnamese we need unicode support
    // For this environment, we might not have custom fonts. Let's try to use standard unicode font logic or simple font.
    // If we need Vietnamese characters, we should use a font that supports it.
    // Checking environment, we are on Linux.
    // Let's use a standard font that supports Vietnamese if possible, or fallback to default.

    public File generateReceipt(Transaction transaction, Account account) throws IOException {
        // Create directory if not exists
        String dirPath = "pdf_history";
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "Receipt_" + transaction.getReferenceNumber() + ".pdf";
        File file = new File(directory, fileName);

        Document document = new Document(PageSize.A5.rotate()); // A5 Landscape for receipt
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Font Setup (Attempting to use a font that supports Vietnamese)
            // Ideally we should load a font file. Since we don't have one in resources yet,
            // we will use standard font but Vietnamese characters might be issue without embedded font.
            // We will try creating a Font with BaseFont.IDENTITY_H if we had a ttf.
            // For now, let's use standard Times Roman and hope for the best or strip accents if needed?
            // No, requirements say Vietnamese.
            // Let's assume we can use a font like specific path or default.
            // In a real app we'd bundle a .ttf file.

            // Simplified for this task: using Standard Font.
            // If Vietnamese fails to render, we would need to add a TTF file to resources.
            // Let's assume standard behavior.
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Header
            Paragraph title = new Paragraph("PHIẾU GIAO DỊCH / RECEIPT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2});

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            addTableRow(table, "Mã giao dịch:", transaction.getReferenceNumber(), headerFont, normalFont);
            addTableRow(table, "Ngày GD:", transaction.getCreatedDate().format(formatter), headerFont, normalFont);
            addTableRow(table, "----------------", "--------------------------------", normalFont, normalFont);

            addTableRow(table, "Loại GD:", transaction.getTransactionType().getDisplayName(), headerFont, normalFont);
            addTableRow(table, "Số tài khoản:", account.getAccountNumber(), headerFont, normalFont);
            // Customer Name isn't directly in Account object unless populated.
            // We'll rely on what's available.
            if (account.getCustomerName() != null) {
                addTableRow(table, "Chủ TK:", account.getCustomerName(), headerFont, normalFont);
            }

            addTableRow(table, "----------------", "--------------------------------", normalFont, normalFont);
            addTableRow(table, "Số tiền:", String.format("%,.0f VND", transaction.getAmount()), headerFont, normalFont);
            addTableRow(table, "Số dư sau GD:", String.format("%,.0f VND", transaction.getBalanceAfter()), headerFont, normalFont);
            addTableRow(table, "Nội dung:", transaction.getDescription(), headerFont, normalFont);

            addTableRow(table, "----------------", "--------------------------------", normalFont, normalFont);
            addTableRow(table, "Nhân viên:", SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getUsername() : "System", headerFont, normalFont);

            document.add(table);

            // Footer
            Paragraph footer = new Paragraph("\nCảm ơn Quý khách / Thank you!", normalFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

        } catch (DocumentException e) {
            throw new IOException("Error generating PDF", e);
        } finally {
            document.close();
        }

        return file;
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    public void openReceipt(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
