module com.adrijavi {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.adrijavi to javafx.fxml;
    exports com.adrijavi;
}
