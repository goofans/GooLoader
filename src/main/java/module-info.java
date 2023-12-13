module me.wy.gooloader.gooloader {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires java.desktop;
    requires java.xml;

    opens me.wy.gooloader.gooloader to javafx.fxml;
    opens me.wy.gooloader.gooloader.util to javafx.fxml;

    exports me.wy.gooloader.gooloader;
}