package representativegui;

import entities.Reservation; // Import your existing class
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * JavaFX controller for displaying today's reservations in a table view.
 * <p>
 * Receives a list of formatted strings from the server, parses them into {@link Reservation}
 * objects, and displays them in a {@link TableView}. Provides a refresh button that triggers
 * a caller-provided {@link Runnable} action.
 */
public class TodaysReservationsController {

    /**
     * Table view displaying today's reservations.
     */
    @FXML private TableView<Reservation> reservationsTable;
    
    /**
     * Column displaying the reservation start time (formatted as {@code HH:mm}).
     */
    @FXML private TableColumn<Reservation, String> colTime;

    /**
     * Column displaying the number of diners.
     */
    @FXML private TableColumn<Reservation, String> colDiners;

    /**
     * Column displaying the assigned table number.
     */
    @FXML private TableColumn<Reservation, String> colTable;

    /**
     * Column displaying the reservation confirmation code.
     */
    @FXML private TableColumn<Reservation, String> colCode;
    
    /**
     * Label used to show status messages (loading state, errors, and totals).
     */
    @FXML private Label statusLabel;

    /**
     * Button that triggers a refresh action provided by the parent controller.
     */
    @FXML private Button refreshBtn;

    /**
     * Action invoked when the refresh button is pressed.
     */
    private Runnable refreshAction;

    /**
     * JavaFX initialization hook.
     * <p>
     * Binds table columns to {@link Reservation} properties and configures the refresh behavior.
     * The time column converts the {@link Timestamp} start time into a string representation.
     */
    @FXML
    public void initialize() {
        // 1. Map Time: Convert Timestamp back to "HH:mm" string for display
        colTime.setCellValueFactory(cellData -> {
            Timestamp ts = cellData.getValue().getStartTime();
            if (ts != null) {
                // Convert Timestamp -> LocalTime -> String
                String formatted = ts.toLocalDateTime().toLocalTime().toString();
                return new SimpleStringProperty(formatted);
            }
            return new SimpleStringProperty("");
        });

        // 2. Map other simple fields (Diners, Table, Code)
        // These names must match your Reservation getters (getDinnersNumber, etc.)
        colDiners.setCellValueFactory(new PropertyValueFactory<>("dinnersNumber"));
        colTable.setCellValueFactory(new PropertyValueFactory<>("tableNumber"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        
        reservationsTable.setPlaceholder(new Label("No reservations found for today"));

        refreshBtn.setOnAction(e -> {
            if(refreshAction != null) refreshAction.run();
        });
    }

    /**
     * Sets the action executed when the refresh button is pressed.
     *
     * @param action the refresh action to invoke
     */
    public void setRefreshAction(Runnable action) {
        this.refreshAction = action;
    }

    /**
     * Parses the server-provided reservation strings and updates the table view.
     * <p>
     * Expected input lines are in the format:
     * {@code "🕒 12:00 | 👥 4 | 🍽️ Table 5 | 🔑 ABC12"}.
     * <p>
     * For each valid line, this method creates a {@link Reservation}, populates key fields,
     * converts the parsed time into a {@link Timestamp} using today's date, and inserts the
     * object into the table.
     *
     * @param rawServerStrings list of formatted reservation strings received from the server
     */
    public void updateTableData(List<String> rawServerStrings) {
        ObservableList<Reservation> data = FXCollections.observableArrayList();

        if (rawServerStrings == null || rawServerStrings.isEmpty()) {
            statusLabel.setText("No data received.");
            reservationsTable.setItems(data);
            return;
        }

        if (rawServerStrings.get(0).startsWith("No reservations") || rawServerStrings.get(0).startsWith("❌")) {
            statusLabel.setText(rawServerStrings.get(0));
            reservationsTable.setItems(data);
            return;
        }

        // PARSE STRINGS INTO RESERVATION OBJECTS
        for (String line : rawServerStrings) {
            try {
                // Format: "🕒 12:00 | 👥 4 | 🍽️ Table 5 | 🔑 ABC12"
                String[] parts = line.split("\\|");

                String timeStr = parts[0].replace("🕒", "").trim();      // "12:00"
                String dinersStr = parts[1].replace("👥", "").trim();    // "4"
                String tableStr = parts[2].replace("🍽️", "").replace("Table", "").trim(); // "5"
                String codeStr = parts[3].replace("🔑", "").trim();      // "ABC12"

                // Create and populate the Reservation object
                Reservation res = new Reservation();
                
                res.setDinnersNumber(Integer.parseInt(dinersStr));
                res.setTableNumber(Integer.parseInt(tableStr));
                res.setConfirmationCode(codeStr);

                // Convert String "12:00" -> SQL Timestamp (assuming Today)
                LocalTime lt = LocalTime.parse(timeStr); 
                LocalDateTime ldt = LocalDateTime.of(LocalDate.now(), lt);
                res.setStartTime(Timestamp.valueOf(ldt));

                data.add(res);
                
            } catch (Exception e) {
                System.err.println("Error parsing line: " + line);
            }
        }

        reservationsTable.setItems(data);
        statusLabel.setText("Loaded " + data.size() + " reservations.");
    }
}
