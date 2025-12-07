package com.kimaita.harambee.controllers;

import com.kimaita.harambee.ItemCreationDialog;
import com.kimaita.harambee.dao.HarambeeDAO;
import com.kimaita.harambee.models.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

public class PledgeController {

    public RadioButton rbImmediate;
    public ToggleGroup deliveryDateGroup;
    public RadioButton rbSchedule;
    public DatePicker dpDeliveryDate;
    @FXML
    private ComboBox<User> cbDonor;
    @FXML
    private ComboBox<InventoryItem> cbItem; // Changed from TextField
    @FXML
    private TextField txtQuantity;
    @FXML
    private Label lblUnit; // Replaces txtUnit
    @FXML
    private Button btnCreateItem, btnAddItem, btnPledge;

    @FXML
    private TableView<PledgeItem> tblItems;
    @FXML
    private TableColumn<PledgeItem, String> colItemName, colUnit;
    @FXML
    private TableColumn<PledgeItem, Integer> colQuantity;

    private final HarambeeDAO dao = new HarambeeDAO();
    private final ObservableList<PledgeItem> pledgeItems = FXCollections.observableArrayList();
    private final ObservableList<InventoryItem> inventoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupInputs();
    }

    private void setupTable() {
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("units"));
        tblItems.setItems(pledgeItems);
    }

    private void setupInputs() {
        // Configure Item ComboBox
        cbItem.setItems(inventoryList);
        cbItem.setConverter(new StringConverter<InventoryItem>() {
            @Override
            public String toString(InventoryItem item) {
                return item == null ? "" : item.getName();
            }

            @Override
            public InventoryItem fromString(String string) {
                return null;
            }
        });

        // Update Unit Label when item selected
        cbItem.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newItem) -> {
            if (newItem != null) {
                lblUnit.setText(newItem.getUnits());
            }
        });

        // Toggle DatePicker availability
        rbImmediate.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            dpDeliveryDate.setDisable(isNowSelected);
            if (isNowSelected) {
                dpDeliveryDate.setValue(LocalDate.now());
            }
        });

        // Default state
        dpDeliveryDate.setDisable(true);
        dpDeliveryDate.setValue(LocalDate.now());

        btnAddItem.setOnAction(e -> handleAddItem());
        btnCreateItem.setOnAction(e -> handleCreateNewItem());
        btnPledge.setOnAction(e -> handleSubmitPledge());
    }

    private void loadData() {
        try {
            cbDonor.setItems(FXCollections.observableArrayList(dao.getUsersByRole(UserType.DONOR)));
            refreshInventoryList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshInventoryList() throws SQLException {
        inventoryList.setAll(dao.getInventoryList());
    }

    private void handleCreateNewItem() {
        Optional<InventoryItem> newItem = ItemCreationDialog.showAndWait();
        newItem.ifPresent(item -> {
            try {
                refreshInventoryList();
                cbItem.getSelectionModel().select(
                        inventoryList.stream().filter(i -> i.getId() == item.getId()).findFirst().orElse(null)
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleAddItem() {
        InventoryItem selected = cbItem.getValue();
        if (selected == null || txtQuantity.getText().isEmpty()) {
            return;
        }

        try {
            int qty = Integer.parseInt(txtQuantity.getText());

            PledgeItem pItem = new PledgeItem(selected.getName(), qty, selected.getUnits());
            pItem.setItemId(selected.getId()); // Store ID for DB reference

            pledgeItems.add(pItem);

            txtQuantity.clear();
            cbItem.getSelectionModel().clearSelection();
            lblUnit.setText("--");
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid Quantity").show();
        }
    }

    private void handleSubmitPledge() {
        if (cbDonor.getValue() == null || pledgeItems.isEmpty()) {
            return;
        }

        DonationPledge pledge = new DonationPledge();
        pledge.setUserId(cbDonor.getValue().getId());
        pledge.setDescription("Donation Pledge");
        pledge.setItems(pledgeItems);

        OffsetDateTime deliveryDate;
        if (rbImmediate.isSelected()) {
            deliveryDate = OffsetDateTime.now();
        } else {
            LocalDate selectedDate = dpDeliveryDate.getValue();
            // Use NOON UTC to avoid timezone shifting issues at midnight
            deliveryDate = selectedDate.atTime(12, 0).atOffset(ZoneOffset.UTC);
        }
        pledgeItems.forEach(i -> i.setDeliveryDate(deliveryDate));

        try {
            dao.createPledge(pledge);
            pledgeItems.clear();
            new Alert(Alert.AlertType.INFORMATION, "Pledge Recorded Successfully!").show();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
        }
    }
}
