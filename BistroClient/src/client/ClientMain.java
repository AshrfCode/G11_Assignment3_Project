package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

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

    public static void main(String[] args) {
        launch(args);
    }
}
