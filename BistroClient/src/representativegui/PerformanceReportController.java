package representativegui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.time.Year;
import java.util.Map;

/**
 * JavaFX controller for generating and displaying a monthly performance report.
 * <p>
 * Requests aggregated reservation timing statistics from the server for a selected month/year
 * and visualizes the results in a {@link PieChart}. Uses {@link ClientSession#activeHandler}
 * to process the asynchronous response.
 */
public class PerformanceReportController {

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Main content container used for navigation back to the visual reports menu.
     */
    private StackPane mainContentArea; // Required to go back to the menu

    /**
     * Combo box for selecting the report month (1-12).
     */
    @FXML private ComboBox<String> monthCombo;

    /**
     * Combo box for selecting the report year (e.g., current and previous year).
     */
    @FXML private ComboBox<String> yearCombo;

    /**
     * Pie chart used to visualize the report statistics.
     */
    @FXML private PieChart pieChart;

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
     * Sets the main content area (StackPane) to allow navigation back to the menu.
     *
     * @param area the container in which this view is displayed
     */
    public void setMainContentArea(StackPane area) {
        this.mainContentArea = area;
    }

    /**
     * JavaFX initialization hook.
     * <p>
     * Populates the month/year selectors with defaults and common options.
     */
    @FXML
    public void initialize() {
        // 1. Setup Month Dropdown (1-12)
        ObservableList<String> months = FXCollections.observableArrayList(
            "1", "2", "3", "4", "5", "6", 
            "7", "8", "9", "10", "11", "12"
        );
        monthCombo.setItems(months);
        monthCombo.getSelectionModel().select("7"); // Default to July (or current month)

        // 2. Setup Year Dropdown (Current Year and Previous Year)
        int currentYear = Year.now().getValue();
        yearCombo.getItems().addAll(
            String.valueOf(currentYear - 1), 
            String.valueOf(currentYear)
        );
        // Default to current year
        yearCombo.getSelectionModel().select(String.valueOf(currentYear)); 
    }

    /**
     * Loads performance data for the selected month/year and updates the chart.
     * <p>
     * Installs an active handler that expects a {@code Map<String, Integer>} from the server,
     * then converts it into pie chart slices.
     */
    @FXML
    private void loadData() {
        if (client == null) {
            statusLabel.setText("❌ No server connection.");
            return;
        }

        String m = monthCombo.getValue();
        String y = yearCombo.getValue();
        
        statusLabel.setText("Generating report for " + m + "/" + y + "...");
        pieChart.setData(FXCollections.emptyObservableList()); // Clear old chart data

        // 3. Define the Handler for the Server Response
        // The server will send back a Map<String, Integer>
        ClientSession.activeHandler = (msg) -> {
            Platform.runLater(() -> {
                if (msg instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> stats = (Map<String, Integer>) msg;
                    
                    updateChart(stats);
                    statusLabel.setText("✅ Report Loaded Successfully");
                } else {
                    statusLabel.setText("❌ Error: Received unexpected data from server.");
                    System.err.println("Expected Map, got: " + msg.getClass().getName());
                }
            });
        };

        // 4. Send Request using the helper method we added to ClientController
        client.requestMonthlyTimeReport(m, y);
    }

    /**
     * Builds and applies pie chart data from the provided statistics map.
     *
     * @param stats map of category name to count (e.g., Normal/Delayed/Extended)
     */
    private void updateChart(Map<String, Integer> stats) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        // Helper to add slice only if value > 0 to keep the chart clean
        addSlice(pieData, "Normal", stats.getOrDefault("Normal", 0));
        addSlice(pieData, "Delayed", stats.getOrDefault("Delayed", 0));
        addSlice(pieData, "Extended", stats.getOrDefault("Extended", 0));

        pieChart.setData(pieData);
    }

    /**
     * Adds a pie slice to the chart data only when the value is greater than zero.
     *
     * @param list  target list of pie chart data
     * @param name  slice/category name
     * @param value slice value (count)
     */
    private void addSlice(ObservableList<PieChart.Data> list, String name, int value) {
        if (value > 0) {
            // Format label like: "Normal (15)"
            list.add(new PieChart.Data(name + " (" + value + ")", value));
        }
    }

    /**
     * Navigates back to the visual reports menu view.
     * <p>
     * Reloads {@code VisualReportsMenu.fxml}, re-injects the client and content container,
     * and swaps the view inside {@link #mainContentArea}.
     */
    @FXML
    private void handleBack() {
        // Go back to the VisualReportsMenu
        if (mainContentArea == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/representativegui/VisualReportsMenu.fxml"));
            Parent root = loader.load();
            
            // Re-inject dependencies into the menu controller
            VisualReportsMenuController controller = loader.getController();
            controller.setClient(client);
            controller.setMainContentArea(mainContentArea);
            
            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(root);
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("❌ Error navigating back.");
        }
    }
}
