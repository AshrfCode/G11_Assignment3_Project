package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX application entry point for the Bistro client.
 * <p>
 * Loads the initial connection screen and displays the primary stage.
 */
public class ClientMain extends Application {

    /**
     * Initializes and shows the primary JavaFX stage for the client application.
     * <p>
     * Loads the {@code ConnectView.fxml} layout, creates the scene, and configures
     * the window properties (title, size behavior, and position).
     *
     * @param primaryStage the primary stage provided by the JavaFX runtime
     * @throws Exception if the FXML resource cannot be loaded or initialization fails
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                ClientMain.class.getResource("/clientgui/ConnectView.fxml")
        );
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Bistro – Connect");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);  
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Application main method.
     * <p>
     * Delegates to {@link #launch(String...)} to start the JavaFX runtime.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}
