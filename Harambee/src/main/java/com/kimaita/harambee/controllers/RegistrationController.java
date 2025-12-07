package com.kimaita.harambee.controllers;

import com.kimaita.harambee.dao.HarambeeDAO;
import com.kimaita.harambee.models.EntityType;
import com.kimaita.harambee.models.User;
import com.kimaita.harambee.models.UserType;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class RegistrationController {

    private final HarambeeDAO dao = new HarambeeDAO();
    @FXML
    private TextField txtName, txtEmail, txtPhone, txtStreet, txtCity, txtState, txtZip;
    @FXML
    private RadioButton rbRecipient, rbDonor, rbPerson, rbOrganization;
    @FXML
    private ToggleGroup userTypeGroup, donorTypeGroup;
    @FXML
    private Button btnRegister;

    @FXML
    public void initialize() {
        btnRegister.setOnAction(e -> register());
    }

    private void register() {
        User user = new User();
        user.setName(txtName.getText());
        user.setEmail(txtEmail.getText());
        user.setPhoneNumber(txtPhone.getText());

        String address = String.format("%s, %s", txtStreet.getText(), txtCity.getText());
        user.setAddress(address);

        user.setRole(rbDonor.isSelected() ? UserType.DONOR : UserType.RECIPIENT);

        if (user.getRole() == UserType.DONOR) {
            user.setEntityType(rbOrganization.isSelected() ? EntityType.ORGANISATION : EntityType.INDIVIDUAL);
        } else {
            // Defaulting for recipient
            user.setEntityType(EntityType.INDIVIDUAL);
        }

        try {
            dao.registerUser(user);
            showAlert(Alert.AlertType.INFORMATION, "Success", "User Registered Successfully!");
            clearFields();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Database Error: " + ex.getMessage());
        }
    }

    private void clearFields() {
        txtName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtStreet.clear();
        txtCity.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}