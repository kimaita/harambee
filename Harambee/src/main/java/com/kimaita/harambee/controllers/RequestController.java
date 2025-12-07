package com.kimaita.harambee.controllers;

import com.kimaita.harambee.ItemCreationDialog;
import com.kimaita.harambee.dao.HarambeeDAO;
import com.kimaita.harambee.models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.Optional;

public class RequestController {

    private final HarambeeDAO dao = new HarambeeDAO();
    private final ObservableList<RequestItem> requestItems = FXCollections.observableArrayList();
    private final ObservableList<InventoryItem> inventoryList = FXCollections.observableArrayList();
    @FXML
    private ComboBox<User> cbRecipient;
    @FXML
    private ComboBox<InventoryItem> cbItem;
    @FXML
    private TextField txtQuantity;
    @FXML
    private Label lblUnit;
    @FXML
    private Button btnCreateItem, btnAddItem, btnSubmit;
    @FXML
    private TextArea txtNotes;
    @FXML
    private DatePicker dpDeadline;
    @FXML
    private CheckBox chkRecurring;
    @FXML
    private TableView<RequestItem> tblItems;
    @FXML
    private TableColumn<RequestItem, String> colItemName, colUnit;
    @FXML
    private TableColumn<RequestItem, Integer> colQuantity;

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
        tblItems.setItems(requestItems);
    }

    private void setupInputs() {
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

        cbItem.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                lblUnit.setText(newVal.getUnits());
            }
        });

        btnAddItem.setOnAction(e -> addItem());
        btnCreateItem.setOnAction(e -> createNewItem());
        btnSubmit.setOnAction(e -> submitRequest());
    }

    private void loadData() {
        try {
            cbRecipient.setItems(FXCollections.observableArrayList(dao.getUsersByRole(UserType.RECIPIENT)));
            inventoryList.setAll(dao.getInventoryList());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createNewItem() {
        Optional<InventoryItem> newItem = ItemCreationDialog.showAndWait();
        newItem.ifPresent(item -> {
            try {
                inventoryList.setAll(dao.getInventoryList());
                cbItem.getSelectionModel().select(inventoryList.stream().filter(i -> i.getId() == item.getId()).findFirst().orElse(null));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void addItem() {
        InventoryItem selected = cbItem.getValue();
        if (selected == null || txtQuantity.getText().isEmpty()) {
            return;
        }

        try {
            int qty = Integer.parseInt(txtQuantity.getText());
            RequestItem rItem = new RequestItem(selected.getName(), qty, selected.getUnits());
            rItem.setItemId(selected.getId());
            // TODO Set deadline for each item

            requestItems.add(rItem);
            txtQuantity.clear();
            cbItem.getSelectionModel().clearSelection();
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid Quantity").show();
        }
    }

    private void submitRequest() {
        if (cbRecipient.getValue() == null || requestItems.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please select recipient and add items.").show();
            return;
        }

        Request req = new Request();
        req.setUserId(cbRecipient.getValue().getId());
        req.setDescription(txtNotes.getText());
        req.setRecurring(chkRecurring.isSelected());

        if (dpDeadline.getValue() != null) {
            requestItems.forEach(rItem ->
                    rItem.setDateNeeded(dpDeadline.getValue()));
        }
        req.getItems().addAll(requestItems);

        try {
            dao.createRequest(req);
            requestItems.clear();
            txtNotes.clear();
            new Alert(Alert.AlertType.INFORMATION, "Request Submitted Successfully!").show();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
        }
    }
}
