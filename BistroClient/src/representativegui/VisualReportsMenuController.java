package representativegui;

import java.io.IOException;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class VisualReportsMenuController {

    private ClientController client;
    private StackPane mainContentArea; // Reference to the main area to load the actual charts later

    public void setClient(ClientController client) {
        this.client = client;
    }

    public void setMainContentArea(StackPane contentArea) {
        this.mainContentArea = contentArea;
    }

    @FXML
    private void showTimeReport() {
        if (mainContentArea == null || client == null) {
            System.err.println("Error: Dependencies not set in VisualReportsMenuController.");
            return;
        }

        try {
            // 1. Load the Monthly Time Report FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/representativegui/PerformanceReportView.fxml"));
            Parent root = loader.load();

            // 2. Get the controller and pass dependencies
            PerformanceReportController controller = loader.getController();
            controller.setClient(this.client);
            controller.setMainContentArea(this.mainContentArea); // Pass this so the "Back" button works

            // 3. Display the view
            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load PerformanceReportView.fxml");
        }
    }

    @FXML
    private void showSubscribersReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/representativegui/SubscriberReportView.fxml"));
            Parent root = loader.load();

            SubscriberReportController controller = loader.getController();
            controller.setClient(this.client);
            controller.setMainContentArea(this.mainContentArea);

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(root);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load SubscriberReportView.fxml");
        }
    }
}