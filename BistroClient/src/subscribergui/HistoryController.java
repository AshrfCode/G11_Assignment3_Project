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

/**
 * JavaFX controller for displaying a subscriber's reservation history.
 * <p>
 * Loads history data from the server (as a {@link SubscriberHistoryResponse}) and presents it
 * in a {@link TableView} of {@link ReservationHistoryRow}. Also displays summary values such
 * as total reservations and visits count.
 */
public class HistoryController {

    /**
     * Connected client controller used to request history data from the server.
     */
    private ClientController client;

    /**
     * Subscriber identifier whose history is being displayed.
     */
    private int subscriberId;

    /**
     * Table displaying reservation history rows.
     */
    @FXML private TableView<ReservationHistoryRow> historyTable;

    /**
     * Column displaying the reservation start time.
     */
    @FXML private TableColumn<ReservationHistoryRow, LocalDateTime> startCol;

    /**
     * Column displaying the reservation end time.
     */
    @FXML private TableColumn<ReservationHistoryRow, LocalDateTime> endCol;

    /**
     * Column displaying the number of diners for the reservation.
     */
    @FXML private TableColumn<ReservationHistoryRow, Integer> dinersCol;

    /**
     * Column displaying the assigned table number.
     */
    @FXML private TableColumn<ReservationHistoryRow, Integer> tableCol;

    /**
     * Column displaying the reservation confirmation code.
     */
    @FXML private TableColumn<ReservationHistoryRow, String> codeCol;

    /**
     * Column displaying the reservation status (e.g., completed/canceled).
     */
    @FXML private TableColumn<ReservationHistoryRow, String> statusCol;

    /**
     * Label displaying the number of visits counted for the subscriber.
     */
    @FXML private Label visitsLabel;

    /**
     * Label displaying the total number of reservations in the history list.
     */
    @FXML private Label totalReservationsLabel;

    /**
     * Label used to show loading state and status messages.
     */
    @FXML private Label statusLabel;

    /**
     * JavaFX initialization hook.
     * <p>
     * Binds table columns to {@link ReservationHistoryRow} properties and applies formatting
     * for start/end date-time columns.
     */
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

    /**
     * Updates the UI with reservation history data received from the server.
     * <p>
     * Populates the table with reservations, updates summary labels, and sets a success status.
     *
     * @param res the history response containing reservations and computed summary values
     */
    public void onHistoryReceived(SubscriberHistoryResponse res) {
        // make sure UI update is on FX thread
        Platform.runLater(() -> {
            historyTable.setItems(FXCollections.observableArrayList(res.getReservations()));
            totalReservationsLabel.setText(String.valueOf(res.getReservations().size()));
            visitsLabel.setText(String.valueOf(res.getVisitsCount()));
            statusLabel.setText("✅ Loaded.");
        });
    }
    
    /**
     * Initializes this controller with the connected client and subscriber context and triggers loading history.
     * <p>
     * Installs an active handler that listens for {@link SubscriberHistoryResponse} and updates the UI when received,
     * then requests the subscriber history from the server.
     *
     * @param client       the connected {@link ClientController} used to request history
     * @param subscriberId the subscriber identifier whose history should be fetched
     */
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
