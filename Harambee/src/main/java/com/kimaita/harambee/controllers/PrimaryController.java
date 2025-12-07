package com.kimaita.harambee.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.kimaita.harambee.App;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

public class PrimaryController implements Initializable {

    @FXML
    private Button btnDisburse;
    @FXML
    private ScrollPane contentScrollPane;
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Button btnHome;
    @FXML
    private Button btnRegister;
    @FXML
    private Button btnPledge;
    @FXML
    private Button btnRequest;
    @FXML
    private Button btnDelivery;
    @FXML
    private Button btnInventory;
    @FXML
    private Button btnOverview;
    @FXML
    private Button btnReports;

    private List<Button> menuButtons;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        menuButtons = new ArrayList<>();
        menuButtons.add(btnHome);
        menuButtons.add(btnRegister);
        menuButtons.add(btnPledge);
        menuButtons.add(btnRequest);
        menuButtons.add(btnDelivery);
        menuButtons.add(btnDisburse);
        menuButtons.add(btnInventory);
        menuButtons.add(btnOverview);
        menuButtons.add(btnReports);

        // Load the default page (Home)
        loadView("home_screen.fxml");
        highlightButton(btnHome);
    }

    @FXML
    void showHome(ActionEvent event) {
        navigate(event, "home_screen.fxml");
    }

    @FXML
    void showRegister(ActionEvent event) {
        navigate(event, "register_user.fxml");
    }

    @FXML
    void showPledge(ActionEvent event) {
        navigate(event, "record_pledge.fxml");
    }

    @FXML
    void showRequest(ActionEvent event) {
        navigate(event, "make_request.fxml");
    }

    @FXML
    void showDelivery(ActionEvent event) {
        navigate(event, "pledge_delivery.fxml");
    }

    @FXML
    void showInventory(ActionEvent event) {
        navigate(event, "inventory.fxml");
    }

    @FXML
    void showOverview(ActionEvent event) {
        navigate(event, "activity.fxml");
    }

    @FXML
    void showReports(ActionEvent event) {
        navigate(event, "reports.fxml");
    }
    @FXML
    void showDisburse(ActionEvent event) { navigate(event, "disbursement.fxml"); }
    /**
     *
     * @param event
     * @param fxmlFile
     */
    private void navigate(ActionEvent event, String fxmlFile) {
        loadView(fxmlFile);
        if (event.getSource() instanceof Button) {
            highlightButton((Button) event.getSource());
        }
    }

    /**
     *
     * @param selectedButton
     */
    private void highlightButton(Button selectedButton) {
        // 1. Reset Styles (Clean, transparent, light gray text)
        String inactiveStyle = "-fx-background-color: transparent; "
                + "-fx-text-fill: #b0b0b0; "
                + "-fx-cursor: hand; "
                + "-fx-border-width: 0 0 0 0;";

        for (Button btn : menuButtons) {
            btn.setStyle(inactiveStyle);
        }

        // 2. Set Active Style (White text, Blue accent bar on left, no boxy background)
        String activeStyle = "-fx-background-color: #242a39; "
                + "-fx-text-fill: #ffffff; "
                + "-fx-border-color: #2196f3; "
                + "-fx-border-width: 0 0 0 4; "
                + "-fx-cursor: hand;";

        selectedButton.setStyle(activeStyle);
    }

    /**
     *
     * @param fxmlFileName
     */
    private void loadView(String fxmlFileName) {
        try {
            Node view = App.loadFXML(fxmlFileName);
            contentScrollPane.setContent(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load FXML file: " + fxmlFileName);
        }
    }


}
