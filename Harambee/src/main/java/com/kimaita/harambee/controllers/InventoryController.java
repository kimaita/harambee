package com.kimaita.harambee.controllers;

import com.kimaita.harambee.dao.HarambeeDAO;
import com.kimaita.harambee.models.InventoryItem;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class InventoryController {

    @FXML
    private Button btnCloseSidebar;
    @FXML
    private VBox sidebarPane;
    @FXML
    private Button btnDefineItem;
    @FXML
    private TableView<InventoryItem> tblInventory;
    @FXML
    private TableColumn<InventoryItem, Integer> colID, colQuantity;
    @FXML
    private TableColumn<InventoryItem, String> colItemName, colCategory, colUnit;
    @FXML
    private TextField txtNewItemName, txtSearch;
    @FXML
    private ComboBox<String> cbNewCategory, cbNewUnit;
    @FXML
    private Button btnAddItem, btnRefresh;

    private final HarambeeDAO dao = new HarambeeDAO();

    @FXML
    public void initialize() {
        // Table Columns
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("units"));

        // Setup Dropdowns
        cbNewUnit.setItems(FXCollections.observableArrayList("kg", "pcs", "box", "liters", "bags"));
        cbNewCategory.setItems(FXCollections.observableArrayList("Food", "Medical", "Clothing", "Tools", "General"));

        // Event Handlers
        btnAddItem.setOnAction(e -> addNewItem());
        btnRefresh.setOnAction(e -> loadInventory());
        btnDefineItem.setOnAction(e -> showSidebar());
        btnCloseSidebar.setOnAction(e -> hideSidebar());

        // Simple Search Filter
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterList(newVal));

        loadInventory();
    }

    private void loadInventory() {
        try {
            tblInventory.setItems(FXCollections.observableArrayList(dao.getAllInventory()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void filterList(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            loadInventory();
            return;
        }
        try {
            // In a real app, use FilteredList wrapper. Here re-fetching for simplicity or filtering existing observable list
            ObservableList<InventoryItem> fullList = FXCollections.observableArrayList(dao.getAllInventory());
            ObservableList<InventoryItem> filtered = fullList.filtered(
                    item -> item.getName().toLowerCase().contains(keyword.toLowerCase())
                    || item.getCategoryName().toLowerCase().contains(keyword.toLowerCase())
            );
            tblInventory.setItems(filtered);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addNewItem() {
        String name = txtNewItemName.getText();
        String cat = cbNewCategory.getValue();
        String unit = cbNewUnit.getValue();

        if (name == null || name.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Item Name is required").show();
            return;
        }

        InventoryItem item = new InventoryItem(name, cat, unit);
        try {
            dao.createInventoryItem(item);
            loadInventory();
            txtNewItemName.clear();
            new Alert(Alert.AlertType.INFORMATION, "Item added to catalog").show();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Could not add item: " + e.getMessage()).show();
        }
    }
    private void showSidebar() {
        sidebarPane.setVisible(true);
        sidebarPane.setManaged(true);
    }

    private void hideSidebar() {
        sidebarPane.setVisible(false);
        sidebarPane.setManaged(false);
    }
}
