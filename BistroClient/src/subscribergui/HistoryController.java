package subscribergui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import client.ClientController;
import client.ClientSession;
import common.ReservationHistoryRow;
import common.SubscriberHistoryResponse;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class HistoryController {

    private ClientController client;
    private int subscriberId;

    @FXML private TableView<ReservationHistoryRow> historyTable;

    @FXML private TableColumn<ReservationHistoryRow, LocalDateTime> startCol;
    @FXML private TableColumn<ReservationHistoryRow, LocalDateTime> endCol;
    @FXML private TableColumn<ReservationHistoryRow, Integer> dinersCol;
    @FXML private TableColumn<ReservationHistoryRow, Integer> tableCol;
    @FXML private TableColumn<ReservationHistoryRow, String> codeCol;
    @FXML private TableColumn<ReservationHistoryRow, String> statusCol;

    @FXML private Label visitsLabel;
    @FXML private Label totalReservationsLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {

        // 🔗 Bind columns to ReservationHistoryRow getters
        startCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("end"));
        dinersCol.setCellValueFactory(new PropertyValueFactory<>("diners"));
        tableCol.setCellValueFactory(new PropertyValueFactory<>("table"));
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 🕒 Date formatter
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // 📅 Format START time
        startCol.setCellFactory(col -> new TableCell<ReservationHistoryRow, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : formatter.format(item));
            }
        });

        // 📅 Format END time
        endCol.setCellFactory(col -> new TableCell<ReservationHistoryRow, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : formatter.format(item));
            }
        });
    }

    public void onHistoryReceived(SubscriberHistoryResponse res) {
        // make sure UI update is on FX thread
        Platform.runLater(() -> {
            historyTable.setItems(FXCollections.observableArrayList(res.getReservations()));
            totalReservationsLabel.setText(String.valueOf(res.getReservations().size()));
            visitsLabel.setText(String.valueOf(res.getVisitsCount()));
            statusLabel.setText("✅ Loaded.");
        });
    }
    
    public void init(ClientController client, int subscriberId) {
        this.client = client;
        this.subscriberId = subscriberId;

        // ✅ Register handler for THIS screen
        ClientSession.activeHandler = (msg) -> {
            if (msg instanceof SubscriberHistoryResponse res) {
                Platform.runLater(() -> onHistoryReceived(res));
            }
        };

        statusLabel.setText("Loading history...");
        client.requestSubscriberHistory(subscriberId);
    }
}
