package representativegui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.Year;
import java.util.Map;

/**
 * JavaFX controller for generating and displaying a subscriber activity report.
 * <p>
 * Requests aggregated subscriber statistics for a selected month/year from the server and
 * visualizes them using a {@link BarChart}. Uses {@link ClientSession#activeHandler} to
 * process the asynchronous server response.
 */
public class SubscriberReportController {

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Main content container used for navigation back to the visual reports menu.
     */
    private StackPane mainContentArea;

    /**
     * Combo box for selecting the report month (1-12).
     */
    @FXML private ComboBox<String> monthCombo;

    /**
     * Combo box for selecting the report year (e.g., current and previous year).
     */
    @FXML private ComboBox<String> yearCombo;

    /**
     * Bar chart used to visualize subscriber report statistics.
     */
    @FXML private BarChart<String, Number> barChart;

    /**
     * Label used to display status messages and errors.
     */
    @FXML private Label statusLabel;

    /**
     * Sets the client instance for server communication.
     *
     * @param client the connected {@link ClientController}
     */
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Sets the main content area to allow navigation back to the menu.
     *
     * @param area the container in which this view is displayed
     */
    public void setMainContentArea(StackPane area) {
        this.mainContentArea = area;
    }

    /**
     * JavaFX initialization hook.
     * <p>
     * Populates month and year selectors with common values and defaults.
     */
    @FXML
    public void initialize() {
        // Setup dropdowns
        ObservableList<String> months = FXCollections.observableArrayList(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"
        );
        monthCombo.setItems(months);
        monthCombo.getSelectionModel().select("1"); // Default January

        int currentYear = Year.now().getValue();
        yearCombo.getItems().addAll(String.valueOf(currentYear - 1), String.valueOf(currentYear));
        yearCombo.getSelectionModel().select(String.valueOf(currentYear));
    }

    /**
     * Loads subscriber report data for the selected month/year and updates the bar chart.
     * <p>
     * Installs an active handler that expects a {@code Map<String, Integer>} from the server.
     */
    @FXML
    private void loadData() {
        if (client == null) {
            statusLabel.setText("❌ No server connection.");
            return;
        }

        String m = monthCombo.getValue();
        String y = yearCombo.getValue();
        
        statusLabel.setText("Loading...");
        barChart.getData().clear(); // Clear old bars

        ClientSession.activeHandler = (msg) -> {
            Platform.runLater(() -> {
                if (msg instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> stats = (Map<String, Integer>) msg;
                    updateChart(stats);
                    statusLabel.setText("✅ Loaded");
                } else {
                    statusLabel.setText("❌ Error receiving data");
                }
            });
        };

        client.requestSubscriberReport(m, y);
    }

    /**
     * Updates the bar chart using the provided statistics map.
     * <p>
     * Expects keys such as {@code "Orders"} and {@code "WaitingList"} and plots them as two series.
     *
     * @param stats map of metric name to count
     */
    private void updateChart(Map<String, Integer> stats) {
        // 1. Clear previous data
        barChart.getData().clear();
        barChart.layout(); // Force layout refresh

        // 2. Create Series 1: Orders (e.g., Orange)
        XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
        ordersSeries.setName("Total Orders");
        int ordersCount = stats.getOrDefault("Orders", 0);
        ordersSeries.getData().add(new XYChart.Data<>("", ordersCount));

        // 3. Create Series 2: Waiting List (e.g., Blue/Red)
        XYChart.Series<String, Number> waitingSeries = new XYChart.Series<>();
        waitingSeries.setName("Waiting List Entries");
        int waitingCount = stats.getOrDefault("WaitingList", 0);
        waitingSeries.getData().add(new XYChart.Data<>("", waitingCount));

        // 4. Add both series to the chart
        barChart.getData().addAll(ordersSeries, waitingSeries);
        
        // Optional: Fix the gap between bars
        barChart.setBarGap(10);
        barChart.setCategoryGap(20);
    }

    /**
     * Navigates back to the visual reports menu view.
     * <p>
     * Reloads {@code VisualReportsMenu.fxml}, re-injects the client and main content container,
     * and swaps the view inside {@link #mainContentArea}.
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/representativegui/VisualReportsMenu.fxml"));
            Parent root = loader.load();
            
            VisualReportsMenuController controller = loader.getController();
            controller.setClient(client);
            controller.setMainContentArea(mainContentArea);
            
            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
