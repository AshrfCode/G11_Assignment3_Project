/**
 * 
 */
/**
 * 
 */
module BistroServer {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    
    opens servergui to javafx.fxml;
    exports servergui;
}