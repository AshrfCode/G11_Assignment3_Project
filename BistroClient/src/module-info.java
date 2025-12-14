/**
 * 
 */
/**
 * 
 */
module BistroClient {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    
    opens client to javafx.graphics, javafx.fxml;          
    opens clientgui to javafx.fxml;                       

    exports client;
    exports clientgui;
}