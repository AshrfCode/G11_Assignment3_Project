package servergui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for the Bistro server graphical user interface.
 * <p>
 * Loads the main server UI from an FXML file and applies the associated stylesheet.
 * </p>
 */
public class ServerGUI extends Application {

    /**
     * Initializes and displays the primary JavaFX stage for the server GUI.
     * <p>
     * Loads {@code ServerMain.fxml}, attaches {@code server.css}, and shows the stage.
     * </p>
     *
     * @param primaryStage the primary stage provided by the JavaFX runtime
     * @throws Exception if the FXML resource cannot be loaded or initialization fails
     */
    @Override
    public void start(@SuppressWarnings("exports") Stage primaryStage) throws Exception {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/servergui/ServerMain.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("server.css").toExternalForm());

        primaryStage.setTitle("Bistro Server");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed to the JavaFX application
     */
    public static void main(String[] args) {
        launch(args);
    }
}
