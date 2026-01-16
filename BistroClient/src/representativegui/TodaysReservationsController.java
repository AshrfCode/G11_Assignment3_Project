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

public class TodaysReservationsController {

    // Use <Reservation> instead of <ReservationEntry>
    @FXML private TableView<Reservation> reservationsTable;
    
    // Columns still display Strings
    @FXML private TableColumn<Reservation, String> colTime;
    @FXML private TableColumn<Reservation, String> colDiners;
    @FXML private TableColumn<Reservation, String> colTable;
    @FXML private TableColumn<Reservation, String> colCode;
    
    @FXML private Label statusLabel;
    @FXML private Button refreshBtn;

    private Runnable refreshAction;

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

    public void setRefreshAction(Runnable action) {
        this.refreshAction = action;
    }

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