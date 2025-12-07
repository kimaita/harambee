package com.kimaita.harambee.controllers;

import com.kimaita.harambee.dao.HarambeeDAO;
import com.kimaita.harambee.models.DonationPledge;
import com.kimaita.harambee.models.PledgeItem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class DeliveryController {

    @FXML private ComboBox<DonationPledge> cbPledgeSelect;
    @FXML private Label lblDonorName, lblPledgeDate, lblRecipientName;
    @FXML private TableView<PledgeItem> tblDeliveryItems;
    @FXML private TableColumn<PledgeItem, String> colItemName, colUnit, colStatus;
    @FXML private TableColumn<PledgeItem, Integer> colQuantity;

    @FXML private ComboBox<String> cbDeliveryStatus;
    @FXML private DatePicker dpActualDeliveryDate;
    @FXML private TextField txtCourier;
    @FXML private TextArea txtRemarks;
    @FXML private Button btnLoadPledge, btnUpdate, btnCancel;

    private final HarambeeDAO dao = new HarambeeDAO();
    private DonationPledge currentPledge;

    @FXML
    public void initialize() {
        // Table Columns
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("units"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status ComboBox
        cbDeliveryStatus.setItems(FXCollections.observableArrayList("Pending", "Delivered", "Cancelled"));
        cbDeliveryStatus.getSelectionModel().select("Pending");

        // Load Pledges into dropdown
        loadPledgeList();

        btnLoadPledge.setOnAction(e -> loadPledgeDetails());
        btnUpdate.setOnAction(e -> updateDelivery());
    }

    private void loadPledgeList() {
        try {
            // Converter to show ID and Donor Name
            cbPledgeSelect.setConverter(new StringConverter<DonationPledge>() {
                @Override public String toString(DonationPledge p) {
                    return p == null ? "" : "ID: " + p.getId() + " - " + p.getDonorName();
                }
                @Override public DonationPledge fromString(String s) { return null; }
            });
            cbPledgeSelect.setItems(FXCollections.observableArrayList(dao.getAllPledges()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadPledgeDetails() {
        currentPledge = cbPledgeSelect.getValue();
        if (currentPledge == null) return;

        try {
            // Fetch full details and items
            DonationPledge fullPledge = dao.getPledgeById(currentPledge.getId());
            lblDonorName.setText(fullPledge.getDonorName());
            lblPledgeDate.setText(fullPledge.getCreatedAt().toLocalDate().toString());
            // Recipient is technically dynamic based on Requests, logic omitted for simplicity
            lblRecipientName.setText("General Pool");

            tblDeliveryItems.setItems(FXCollections.observableArrayList(dao.getPledgeItems(currentPledge.getId())));

        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateDelivery() {
        if (currentPledge == null) return;

        String status = cbDeliveryStatus.getValue();
        if ("Delivered".equals(status)) {
            LocalDate date = dpActualDeliveryDate.getValue();
            if (date == null) {
                new Alert(Alert.AlertType.WARNING, "Please select Actual Delivery Date").show();
                return;
            }

            try {
                // For simplicity, mark ALL items in the table as delivered
                for (PledgeItem item : tblDeliveryItems.getItems()) {
                    if (!"Delivered".equals(item.getStatus())) {
                        dao.recordDelivery(
                                item.getId(),
                                currentPledge.getUserId(),
                                item.getQuantity(),
                                date.atTime(12, 0).atOffset(ZoneOffset.UTC)
                        );
                    }
                }
                new Alert(Alert.AlertType.INFORMATION, "Delivery Updated & Inventory Stocked!").show();
                loadPledgeDetails(); // Refresh table
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Update failed: " + e.getMessage()).show();
            }
        }
    }
}
