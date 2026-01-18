package representativegui;

import java.io.IOException;

import client.ClientController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

/**
 * JavaFX controller for the visual reports menu.
 * <p>
 * Provides navigation actions to load specific visual report views (charts) into the main
 * content area. Requires a connected {@link ClientController} and a reference to the
 * {@link StackPane} container where report views should be displayed.
 */
public class VisualReportsMenuController {

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Reference to the main area used to display report views.
     */
    private StackPane mainContentArea; // Reference to the main area to load the actual charts later

    /**
     * Sets the client instance used for server communication.
     *
     * @param client the connected {@link ClientController}
     */
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Sets the container in which report views will be loaded.
     *
     * @param contentArea the main content {@link StackPane}
     */
    public void setMainContentArea(StackPane contentArea) {
        this.mainContentArea = contentArea;
    }

    /**
     * Loads and displays the monthly time performance report view.
     * <p>
     * Injects the connected {@link ClientController} and main content area reference into the
     * loaded {@link PerformanceReportController} so that the report can request data and the
     * back navigation can return to this menu.
     */
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

    /**
     * Loads and displays the subscribers report view.
     * <p>
     * Injects the connected {@link ClientController} and main content area reference into the
     * loaded {@link SubscriberReportController} so that the report can request data and the
     * back navigation can return to this menu.
     */
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
