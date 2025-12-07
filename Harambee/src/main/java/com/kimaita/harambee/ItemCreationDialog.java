/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.kimaita.harambee;

import com.kimaita.harambee.dao.HarambeeDAO;
import com.kimaita.harambee.models.InventoryItem;
import java.util.Optional;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class ItemCreationDialog {

    public static Optional<InventoryItem> showAndWait() {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Create New Inventory Item");
        dialog.setHeaderText("Define a new item type");

        ButtonType loginButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField name = new TextField();
        name.setPromptText("Item Name");
        ComboBox<String> unit = new ComboBox<>();
        unit.getItems().addAll("kg", "liters", "box", "pcs", "bags");
        unit.setEditable(true);
        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("Food", "Medical", "Clothing", "Tools", "General");
        category.setEditable(true);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Unit:"), 0, 1);
        grid.add(unit, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(category, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new InventoryItem(name.getText(), category.getValue(), unit.getValue());
            }
            return null;
        });

        Optional<InventoryItem> result = dialog.showAndWait();

        // If user clicked Create, save to DB immediately
        if (result.isPresent()) {
            try {
                HarambeeDAO dao = new HarambeeDAO();
                return Optional.of(dao.createInventoryItem(result.get()));
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Failed to create item: " + e.getMessage()).show();
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
