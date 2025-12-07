package com.kimaita.harambee.controllers;


import com.kimaita.harambee.dao.HarambeeDAO;
import com.kimaita.harambee.models.DonationPledge;
import com.kimaita.harambee.models.PledgeItem;
import com.kimaita.harambee.models.User;
import com.kimaita.harambee.models.UserType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportsController {

    // --- TAB 1: RECEIPTS ---
    @FXML private TextField txtPledgeID;
    @FXML private Button btnSearchPledge, btnGeneratePDF;
    @FXML private TextArea txtReceiptPreview;
    @FXML private CheckBox chkEmailReceipt;

    // --- TAB 2: REPORTS ---
    @FXML private ComboBox<User> cbReportUser;
    @FXML private ComboBox<String> cbUserReportType;
    @FXML private DatePicker dpUserFrom, dpUserTo;
    @FXML private Button btnGenUserReport;

    @FXML private ComboBox<String> cbAdminReportType;
    @FXML private CheckBox chkFormatPDF, chkFormatCSV;
    //TODO We are treating PDF as Text File for simplicity
    @FXML private Button btnGenAdminReport;

    private final HarambeeDAO dao = new HarambeeDAO();
    private DonationPledge currentPledgeForReceipt;

    @FXML
    public void initialize() {
        setupReceiptTab();
        setupReportsTab();
    }

    private void setupReceiptTab() {
        btnSearchPledge.setOnAction(e -> searchPledge());
        btnGeneratePDF.setOnAction(e -> saveReceiptToFile());
    }

    private void setupReportsTab() {
        // Load Users
        try {
            List<User> allUsers = dao.getUsersByRole(UserType.DONOR);
            allUsers.addAll(dao.getUsersByRole(UserType.RECIPIENT));
            cbReportUser.setItems(FXCollections.observableArrayList(allUsers));
            cbReportUser.setConverter(new StringConverter<User>() {
                @Override public String toString(User u) { return u == null ? "" : u.getName() + " (" + u.getRole() + ")"; }
                @Override public User fromString(String s) { return null; }
            });
        } catch (SQLException e) { e.printStackTrace(); }

        // Load Report Types
        cbUserReportType.setItems(FXCollections.observableArrayList("Activity History", "Donation Summary"));
        cbAdminReportType.setItems(FXCollections.observableArrayList("Current Inventory Level", "Pending Pledges"));

        // Set Default Dates
        dpUserFrom.setValue(LocalDate.now().minusMonths(1));
        dpUserTo.setValue(LocalDate.now());

        btnGenUserReport.setOnAction(e -> generateUserReport());
        btnGenAdminReport.setOnAction(e -> generateAdminReport());
    }

    // ==========================================
    // RECEIPTS LOGIC
    // ==========================================

    private void searchPledge() {
        String input = txtPledgeID.getText();
        if (input.isEmpty()) return;

        try {
            int id = Integer.parseInt(input);
            currentPledgeForReceipt = dao.getPledgeById(id);

            if (currentPledgeForReceipt != null) {
                List<PledgeItem> items = dao.getPledgeItems(id);
                String receiptText = buildReceiptString(currentPledgeForReceipt, items);
                txtReceiptPreview.setText(receiptText);
            } else {
                txtReceiptPreview.setText("Pledge ID not found.");
                currentPledgeForReceipt = null;
            }

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid ID format").show();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String buildReceiptString(DonationPledge p, List<PledgeItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("************************************\n");
        sb.append("       DONATION RECEIPT            \n");
        sb.append("       Harambee Systems            \n");
        sb.append("************************************\n\n");
        sb.append("Receipt ID:   ").append("RCP-").append(p.getId()).append("\n");
        sb.append("Date:         ").append(p.getCreatedAt().toLocalDate()).append("\n");
        sb.append("Donor:        ").append(p.getDonorName()).append("\n\n");
        sb.append("--- ITEMS PLEDGED ---\n");

        for (PledgeItem item : items) {
            sb.append(String.format("- %-20s : %d %s\n", item.getItemName(), item.getQuantity(), item.getUnits()));
        }

        sb.append("\n************************************\n");
        sb.append("Thank you for your generosity!\n");
        sb.append("************************************");
        return sb.toString();
    }

    private void saveReceiptToFile() {
        if (currentPledgeForReceipt == null || txtReceiptPreview.getText().isEmpty()) return;
        saveTextToFile("Receipt-" + currentPledgeForReceipt.getId(), txtReceiptPreview.getText());
    }

    // ==========================================
    // REPORTS LOGIC
    // ==========================================

    private void generateUserReport() {
        User user = cbReportUser.getValue();
        LocalDate from = dpUserFrom.getValue();
        LocalDate to = dpUserTo.getValue();

        if (user == null || from == null || to == null) {
            new Alert(Alert.AlertType.WARNING, "Please select user and date range.").show();
            return;
        }

        StringBuilder report = new StringBuilder();
        report.append("USER ACTIVITY REPORT\n");
        report.append("User: ").append(user.getName()).append("\n");
        report.append("Role: ").append(user.getRole()).append("\n");
        report.append("Period: ").append(from).append(" to ").append(to).append("\n");
        report.append("--------------------------------------------------\n\n");

        try {
            if (user.getRole() == UserType.DONOR) {
                List<DonationPledge> pledges = dao.getPledgesByDateRange(user.getId(), from, to);
                if (pledges.isEmpty()) report.append("No activity found in this period.");
                for (DonationPledge p : pledges) {
                    report.append(String.format("[%s] Pledge #%d: %s\n",
                            p.getCreatedAt().toLocalDate(), p.getId(), p.getItemsSummary()));
                }
            } else {
                List<String> disbursements = dao.getDisbursementsByDateRange(user.getId(), from, to);
                if (disbursements.isEmpty()) report.append("No activity found in this period.");
                for (String line : disbursements) {
                    report.append(line).append("\n");
                }
            }

            saveTextToFile(user.getName() + "_Report", report.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void generateAdminReport() {
        String type = cbAdminReportType.getValue();
        if (type == null) return;

        StringBuilder report = new StringBuilder();
        report.append("ADMINISTRATIVE REPORT: ").append(type.toUpperCase()).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n");
        report.append("=========================================\n\n");

        try {
            if (type.equals("Current Inventory Level")) {
                List<String> inventory = dao.getInventoryReportData();
                for (String line : inventory) {
                    report.append(line).append("\n");
                }
            } else {
                report.append("Data extraction for this report type is not implemented yet.");
            }

            saveTextToFile("Admin_Report_" + LocalDate.now(), report.toString());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // UTILS
    // ==========================================

    private void saveTextToFile(String defaultName, String content) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName(defaultName + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        // Get the Stage from any control
        Stage stage = (Stage) btnGenAdminReport.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
                new Alert(Alert.AlertType.INFORMATION, "Report saved successfully!").show();
            } catch (IOException ex) {
                new Alert(Alert.AlertType.ERROR, "Error saving file.").show();
                ex.printStackTrace();
            }
        }
    }
}
