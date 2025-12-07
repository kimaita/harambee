package com.kimaita.harambee;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {


    /**
     * @param fxml Filename including extension
     * @return Parent
     * @throws IOException
     */
    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(loadFXML("primary.fxml"), 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Donations Management System");
        stage.show();
    }

}
