module com.kimaita.harambee {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.zaxxer.hikari;

    opens com.kimaita.harambee to javafx.fxml;
    exports com.kimaita.harambee;
    exports com.kimaita.harambee.controllers;
    opens com.kimaita.harambee.controllers to javafx.fxml;
    opens com.kimaita.harambee.models to javafx.base;
}
