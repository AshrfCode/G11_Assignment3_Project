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

public class PerformanceReportController {

    private ClientController client;
    private StackPane mainContentArea; // Required to go back to the menu

    @FXML private ComboBox<String> monthCombo;
    @FXML private ComboBox<String> yearCombo;
    @FXML private PieChart pieChart;
    @FXML private Label statusLabel;

    /**
     * Sets the client instance for server communication.
     */
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Sets the main content area (StackPane) to allow navigation back to the menu.
     */
    public void setMainContentArea(StackPane area) {
        this.mainContentArea = area;
    }

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

    private void updateChart(Map<String, Integer> stats) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        // Helper to add slice only if value > 0 to keep the chart clean
        addSlice(pieData, "Normal", stats.getOrDefault("Normal", 0));
        addSlice(pieData, "Delayed", stats.getOrDefault("Delayed", 0));
        addSlice(pieData, "Extended", stats.getOrDefault("Extended", 0));

        pieChart.setData(pieData);
    }

    private void addSlice(ObservableList<PieChart.Data> list, String name, int value) {
        if (value > 0) {
            // Format label like: "Normal (15)"
            list.add(new PieChart.Data(name + " (" + value + ")", value));
        }
    }

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