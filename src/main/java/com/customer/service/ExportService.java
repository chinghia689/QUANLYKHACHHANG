package com.customer.service;

import com.customer.model.Account;
import com.customer.model.Loan;
import com.customer.model.Role;
import com.customer.model.Transaction;
import com.customer.model.dto.*;
import com.customer.util.SessionManager;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ExportService {

    private static final String DIR_PATH = "pdf_history/reports";
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font SMALL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 8);

    public ExportService() {
        File dir = new File(DIR_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void checkPermission() {
        if (!SessionManager.hasRole(Role.MANAGER, Role.ADMIN)) {
            throw new SecurityException("Access Denied: Insufficient permissions to export reports.");
        }
    }

    private File createPdfFile(String reportName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = reportName + "_" + timestamp + ".pdf";
        return new File(DIR_PATH, fileName);
    }

    // --- Dashboard Report ---

    public File exportDashboardReport(DashboardStats stats) throws IOException {
        checkPermission();
        File file = createPdfFile("DashboardReport");

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            addReportHeader(document, "DASHBOARD OVERVIEW REPORT");
            addMetadata(document);

            // Summary Section
            document.add(new Paragraph("Summary Metrics", HEADER_FONT));
            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.setWidthPercentage(100);
            summaryTable.setSpacingBefore(10);
            summaryTable.setSpacingAfter(20);

            addCell(summaryTable, "Total Customers", HEADER_FONT);
            addCell(summaryTable, "Total Balance", HEADER_FONT);
            addCell(summaryTable, "Total Loans", HEADER_FONT);
            addCell(summaryTable, "Today's Txns", HEADER_FONT);

            addCell(summaryTable, String.valueOf(stats.getTotalCustomers()), NORMAL_FONT);
            addCell(summaryTable, formatMoney(stats.getTotalBalance()), NORMAL_FONT);
            addCell(summaryTable, formatMoney(stats.getTotalLoans()), NORMAL_FONT);
            addCell(summaryTable, String.valueOf(stats.getTodayTransactions()), NORMAL_FONT);

            document.add(summaryTable);

            // Customer Distribution
            document.add(new Paragraph("Customer Distribution", HEADER_FONT));
            PdfPTable custTable = new PdfPTable(2);
            custTable.setWidthPercentage(100);
            custTable.setSpacingBefore(5);
            stats.getCustomerDistribution().forEach((type, count) -> {
                addCell(custTable, type.getDisplayName(), NORMAL_FONT);
                addCell(custTable, String.valueOf(count), NORMAL_FONT);
            });
            document.add(custTable);
            document.add(Chunk.NEWLINE);

            // Account Distribution
            document.add(new Paragraph("Account Type Distribution", HEADER_FONT));
            PdfPTable accTable = new PdfPTable(2);
            accTable.setWidthPercentage(100);
            accTable.setSpacingBefore(5);
            stats.getAccountTypeDistribution().forEach((type, count) -> {
                addCell(accTable, type.toString(), NORMAL_FONT);
                addCell(accTable, String.valueOf(count), NORMAL_FONT);
            });
            document.add(accTable);
            document.add(Chunk.NEWLINE);

            // Top Accounts
            document.add(new Paragraph("Top 5 Accounts by Balance", HEADER_FONT));
            PdfPTable topTable = new PdfPTable(3);
            topTable.setWidthPercentage(100);
            topTable.setSpacingBefore(5);
            addCell(topTable, "Account Number", HEADER_FONT);
            addCell(topTable, "Customer", HEADER_FONT);
            addCell(topTable, "Balance", HEADER_FONT);

            for (Account acc : stats.getTopAccounts()) {
                addCell(topTable, acc.getAccountNumber(), NORMAL_FONT);
                addCell(topTable, acc.getCustomerName(), NORMAL_FONT);
                addCell(topTable, formatMoney(acc.getBalance()), NORMAL_FONT);
            }
            document.add(topTable);

        } catch (DocumentException e) {
            throw new IOException("Error generating PDF", e);
        } finally {
            document.close();
        }
        return file;
    }

    // --- Transaction Report ---

    public File exportTransactionReport(TransactionReportData data) throws IOException {
        checkPermission();
        File file = createPdfFile("TransactionReport");

        Document document = new Document(PageSize.A4.rotate()); // Landscape for wide tables
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            addReportHeader(document, "TRANSACTION REPORT");
            addMetadata(document);

            // Filter Info
            document.add(new Paragraph(String.format("Period: %s to %s", data.getFromDate(), data.getToDate()), NORMAL_FONT));
            if (data.getFilterType() != null) {
                document.add(new Paragraph("Filter Type: " + data.getFilterType(), NORMAL_FONT));
            }
            if (data.getAccountNumber() != null) {
                document.add(new Paragraph("Account ID: " + data.getAccountNumber(), NORMAL_FONT));
            }
            document.add(Chunk.NEWLINE);

            // Summary
            PdfPTable sumTable = new PdfPTable(4);
            sumTable.setWidthPercentage(100);
            addCell(sumTable, "Total Count: " + data.getTotalCount(), HEADER_FONT);
            addCell(sumTable, "Total Amount: " + formatMoney(data.getTotalAmount()), HEADER_FONT);
            addCell(sumTable, "Total Deposits: " + formatMoney(data.getDepositTotal()), HEADER_FONT);
            addCell(sumTable, "Total Withdrawals: " + formatMoney(data.getWithdrawTotal()), HEADER_FONT);
            document.add(sumTable);
            document.add(Chunk.NEWLINE);

            // Data Table
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 2, 2, 2, 2, 3});

            addCell(table, "Date", HEADER_FONT);
            addCell(table, "Ref Number", HEADER_FONT);
            addCell(table, "Account", HEADER_FONT);
            addCell(table, "Type", HEADER_FONT);
            addCell(table, "Amount", HEADER_FONT);
            addCell(table, "Target/Source", HEADER_FONT);
            addCell(table, "Description", HEADER_FONT);

            for (Transaction txn : data.getTransactions()) {
                addCell(table, txn.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), SMALL_FONT);
                addCell(table, txn.getReferenceNumber(), SMALL_FONT);
                addCell(table, txn.getAccountNumber(), SMALL_FONT);
                addCell(table, txn.getTransactionType().name(), SMALL_FONT);
                addCell(table, formatMoney(txn.getAmount()), SMALL_FONT);
                addCell(table, txn.getTargetAccountNumber() != null ? txn.getTargetAccountNumber() : "-", SMALL_FONT);
                addCell(table, txn.getDescription(), SMALL_FONT);
            }
            document.add(table);

        } catch (DocumentException e) {
            throw new IOException("Error generating PDF", e);
        } finally {
            document.close();
        }
        return file;
    }

    // --- Loan Report ---

    public File exportLoanReport(LoanReportData data) throws IOException {
        checkPermission();
        File file = createPdfFile("LoanPortfolioReport");

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            addReportHeader(document, "LOAN PORTFOLIO REPORT");
            addMetadata(document);

            document.add(new Paragraph(String.format("Period: %s to %s", data.getFromDate(), data.getToDate()), NORMAL_FONT));
            document.add(Chunk.NEWLINE);

            // Summary
            document.add(new Paragraph("Total Loans: " + data.getTotalLoans(), HEADER_FONT));
            document.add(new Paragraph("Total Outstanding: " + formatMoney(data.getTotalOutstanding()), HEADER_FONT));
            document.add(Chunk.NEWLINE);

            // Status Distribution Table
            PdfPTable distTable = new PdfPTable(2);
            distTable.setWidthPercentage(50);
            distTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            addCell(distTable, "Status", HEADER_FONT);
            addCell(distTable, "Count", HEADER_FONT);
            data.getStatusDistribution().forEach((status, count) -> {
                addCell(distTable, status.name(), NORMAL_FONT);
                addCell(distTable, String.valueOf(count), NORMAL_FONT);
            });
            document.add(distTable);
            document.add(Chunk.NEWLINE);

            // Loans Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            addCell(table, "ID", HEADER_FONT);
            addCell(table, "Customer", HEADER_FONT);
            addCell(table, "Amount", HEADER_FONT);
            addCell(table, "Start Date", HEADER_FONT);
            addCell(table, "Term (M)", HEADER_FONT);
            addCell(table, "Status", HEADER_FONT);

            for (Loan loan : data.getLoans()) {
                addCell(table, String.valueOf(loan.getId()), SMALL_FONT);
                addCell(table, loan.getCustomerName(), SMALL_FONT);
                addCell(table, formatMoney(loan.getPrincipalAmount()), SMALL_FONT);
                addCell(table, loan.getStartDate().toString(), SMALL_FONT);
                addCell(table, String.valueOf(loan.getTermMonths()), SMALL_FONT);
                addCell(table, loan.getStatus().name(), SMALL_FONT);
            }
            document.add(table);

        } catch (DocumentException e) {
            throw new IOException("Error generating PDF", e);
        } finally {
            document.close();
        }
        return file;
    }

    // --- Account Statement ---

    public File exportAccountStatement(AccountStatementData data) throws IOException {
        checkPermission();
        File file = createPdfFile("AccountStatement_" + data.getAccount().getAccountNumber());

        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            addReportHeader(document, "ACCOUNT STATEMENT");
            addMetadata(document);

            // Account Info
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            addCell(infoTable, "Account Number: " + data.getAccount().getAccountNumber(), HEADER_FONT);
            addCell(infoTable, "Customer: " + data.getAccount().getCustomerName(), HEADER_FONT);
            addCell(infoTable, "Period: " + data.getFromDate() + " to " + data.getToDate(), NORMAL_FONT);
            addCell(infoTable, "Generated At: " + data.getGeneratedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), NORMAL_FONT);
            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Balances
            PdfPTable balTable = new PdfPTable(4);
            balTable.setWidthPercentage(100);
            addCell(balTable, "Opening Balance", HEADER_FONT);
            addCell(balTable, "Total Deposits", HEADER_FONT);
            addCell(balTable, "Total Withdrawals", HEADER_FONT);
            addCell(balTable, "Closing Balance", HEADER_FONT);

            addCell(balTable, formatMoney(data.getOpeningBalance()), NORMAL_FONT);
            addCell(balTable, formatMoney(data.getTotalDeposits()), NORMAL_FONT);
            addCell(balTable, formatMoney(data.getTotalWithdrawals()), NORMAL_FONT);
            addCell(balTable, formatMoney(data.getClosingBalance()), HEADER_FONT);
            document.add(balTable);
            document.add(Chunk.NEWLINE);

            // Transactions
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 2, 2, 3});

            addCell(table, "Date", HEADER_FONT);
            addCell(table, "Ref Number", HEADER_FONT);
            addCell(table, "Description", HEADER_FONT);
            addCell(table, "Debit (-)", HEADER_FONT);
            addCell(table, "Credit (+)", HEADER_FONT);

            for (Transaction txn : data.getTransactions()) {
                addCell(table, txn.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), SMALL_FONT);
                addCell(table, txn.getReferenceNumber(), SMALL_FONT);
                addCell(table, txn.getDescription(), SMALL_FONT);

                boolean isCredit = false;
                if (txn.getTransactionType().name().equals("DEPOSIT") || txn.getTransactionType().name().equals("LOAN_DISBURSEMENT")) {
                    isCredit = true;
                } else if (txn.getTransactionType().name().equals("TRANSFER") && txn.getTargetAccountId() == data.getAccount().getId()) {
                    isCredit = true;
                }

                if (isCredit) {
                    addCell(table, "", SMALL_FONT);
                    addCell(table, formatMoney(txn.getAmount()), SMALL_FONT);
                } else {
                    addCell(table, formatMoney(txn.getAmount()), SMALL_FONT);
                    addCell(table, "", SMALL_FONT);
                }
            }
            document.add(table);

        } catch (DocumentException e) {
            throw new IOException("Error generating PDF", e);
        } finally {
            document.close();
        }
        return file;
    }

    // --- Helpers ---

    private void addReportHeader(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph("CUSTOMER MANAGEMENT SYSTEM", SMALL_FONT);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);

        Paragraph p2 = new Paragraph(title, TITLE_FONT);
        p2.setAlignment(Element.ALIGN_CENTER);
        p2.setSpacingAfter(10);
        doc.add(p2);
        doc.add(new Paragraph("------------------------------------------------------------------------------"));
        doc.add(Chunk.NEWLINE);
    }

    private void addMetadata(Document doc) throws DocumentException {
        String user = SessionManager.getCurrentUser() != null ? SessionManager.getCurrentUser().getFullName() : "System";
        Paragraph p = new Paragraph("Generated by: " + user + " | Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), SMALL_FONT);
        p.setAlignment(Element.ALIGN_RIGHT);
        doc.add(p);
        doc.add(Chunk.NEWLINE);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(4);
        table.addCell(cell);
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0 VND";
        return String.format("%,.0f VND", amount);
    }

    public void openPdf(File file) {
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
