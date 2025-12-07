package com.kimaita.harambee.controllers;

import com.kimaita.harambee.dao.HarambeeDAO;
import com.kimaita.harambee.models.Request;
import com.kimaita.harambee.models.RequestItem;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class DisbursementController {

    @FXML private ComboBox<Request> cbRequestSelect;
    @FXML private Label lblRecipientName, lblRequestDate, lblStatus;

    @FXML private TableView<RequestItem> tblRequestItems;
    @FXML private TableColumn<RequestItem, String> colItemName, colUnit;
    @FXML private TableColumn<RequestItem, Integer> colRequested, colFulfilled, colRemaining;

    @FXML private TextField txtDisburseQty;
    @FXML private DatePicker dpDateGiven;
    @FXML private Button btnLoadRequest, btnDisburse;

    private final HarambeeDAO dao = new HarambeeDAO();
    private Request currentRequest;

    @FXML
    public void initialize() {
        // Setup Columns
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("units"));
        colRequested.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colFulfilled.setCellValueFactory(new PropertyValueFactory<>("fulfilledQuantity"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingQuantity"));

        dpDateGiven.setValue(LocalDate.now());

        loadOpenRequests();

        btnLoadRequest.setOnAction(e -> loadRequestDetails());
        btnDisburse.setOnAction(e -> handleDisbursement());
    }

    private void loadOpenRequests() {
        try {
            cbRequestSelect.setItems(FXCollections.observableArrayList(dao.getAllRequests()));
            cbRequestSelect.setConverter(new StringConverter<Request>() {
                @Override
                public String toString(Request r) {
                    return r == null ? "" : "ID: " + r.getId() + " - " + r.getRecipientName();
                }
                @Override
                public Request fromString(String s) { return null; }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRequestDetails() {
        currentRequest = cbRequestSelect.getValue();
        if (currentRequest == null) return;

        lblRecipientName.setText(currentRequest.getRecipientName());
        lblRequestDate.setText(currentRequest.getCreatedAt().toLocalDate().toString());

        try {
            tblRequestItems.setItems(FXCollections.observableArrayList(
                    dao.getRequestItemsWithStatus(currentRequest.getId())
            ));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDisbursement() {
        RequestItem selectedItem = tblRequestItems.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            new Alert(Alert.AlertType.WARNING, "Please select an item from the table to disburse.").show();
            return;
        }

        if (txtDisburseQty.getText().isEmpty() || dpDateGiven.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Please enter quantity and date.").show();
            return;
        }

        try {
            int qtyToGive = Integer.parseInt(txtDisburseQty.getText());

            // Validation 1: Don't give more than needed
            if (qtyToGive > selectedItem.getRemainingQuantity()) {
                new Alert(Alert.AlertType.WARNING, "Quantity exceeds remaining need.").show();
                return;
            }

            // Validation 2: Don't give more than we have in stock
            int currentStock = dao.getInventoryStock(selectedItem.getItemId());
            if (qtyToGive > currentStock) {
                new Alert(Alert.AlertType.ERROR, "Insufficient Inventory! Current Stock: " + currentStock).show();
                return;
            }

            // Execute
            // Convert LocalDate to Epoch Seconds (BigInt in DB)
            long dateEpoch = dpDateGiven.getValue().atStartOfDay(ZoneOffset.UTC).toEpochSecond();

            dao.recordDisbursement(selectedItem.getId(), currentRequest.getUserId(), qtyToGive, dateEpoch);

            new Alert(Alert.AlertType.INFORMATION, "Disbursement Recorded.").show();
            txtDisburseQty.clear();
            loadRequestDetails(); // Refresh table to show updated progress

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid Quantity").show();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
        }
    }
}
