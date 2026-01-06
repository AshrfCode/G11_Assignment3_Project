package representativegui;

import common.UserRole;
import java.util.List;

import client.ClientController;
import client.ClientSession;
import common.ClientRequest;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import client.ClientController;


public class representativeMainController {
	
	private ClientController client;

    public void setClient(ClientController client) {
        this.client = client;
    }


    public enum EntryMode {
        HOME,
        RESTAURANT
    }
    
    private UserRole role = UserRole.REPRESENTATIVE;

    @FXML private Label sectionTitle;
    @FXML private Label modeBadge;
    @FXML private Label heyUserLabel;

    @FXML private StackPane contentArea;

    @FXML private Button waitingListBtn;
    @FXML private Button reportsBtn;


    private EntryMode entryMode = EntryMode.HOME;
    private String username = "Subscriber";

    @FXML
    public void initialize() {
        applyModeUI();
        applyRoleUI();
        setUsername(username);
        showReservations(); // default page (you can change)
    }
    
    @FXML
    private void showManageTables() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/representativegui/ManageTables.fxml")
            );
            javafx.scene.Parent root = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setEntryMode(EntryMode mode) {
        this.entryMode = mode;
        applyModeUI();
    }

    public void setUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.username = username.trim();
        }
        heyUserLabel.setText("Hey " + this.username);
    }
    
    public void setRole(UserRole role) {
        if (role != null) {
            this.role = role;
        }
        applyRoleUI();
        applyModeUI();
    }


    private void applyModeUI() {
        boolean isRestaurant = (entryMode == EntryMode.RESTAURANT);

        String modeText = isRestaurant ? "Restaurant Mode" : "Home Mode";
        String roleText = (role != null) ? role.name() : "UNKNOWN";

        if (modeBadge != null) {
            modeBadge.setText(roleText + " | " + modeText);
        }

        if (waitingListBtn != null) {
            waitingListBtn.setVisible(isRestaurant);
            waitingListBtn.setManaged(isRestaurant);
        }
    }
    
    
    // ---------------- NAV ACTIONS ----------------
    @FXML
    private void showReservations() {
        setSection("Today's Reservations");

        if (client == null) {
            setPlaceholder("❌ No server connection (client is null).");
            return;
        }

        setPlaceholder("Loading today's reservations...");

        ListView<String> listView = new ListView<>();
        contentArea.getChildren().setAll(listView);

        ClientSession.activeHandler = (msg) -> {
            if (msg instanceof List<?> list) {
                Platform.runLater(() -> {
                    listView.getItems().clear();

                    if (list.isEmpty()) {
                        listView.getItems().add("No reservations for today ✅");
                        return;
                    }

                    if (list.get(0) instanceof String) {
                        @SuppressWarnings("unchecked")
                        List<String> items = (List<String>) list;
                        listView.getItems().addAll(items);
                    } else {
                        listView.getItems().add("⚠️ Unexpected data from server.");
                    }
                });
            }
        };

        client.sendRequest(new ClientRequest(ClientRequest.CMD_GET_TODAY_RESERVATIONS, new Object[]{}));
    }

    
    private VBox buildSpecialOpeningPane() {

        Label specialTitle = new Label("Special Opening (specific date)");
        specialTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Choose date");

        CheckBox closedCheck = new CheckBox("Closed all day");

        TextField openField = new TextField();
        openField.setPromptText("Open time (HH:MM) e.g. 10:00");

        TextField closeField = new TextField();
        closeField.setPromptText("Close time (HH:MM) e.g. 22:00");

        closedCheck.setOnAction(e -> {
            boolean closed = closedCheck.isSelected();
            openField.setDisable(closed);
            closeField.setDisable(closed);
            if (closed) {
                openField.clear();
                closeField.clear();
            }
        });

        Label status = new Label();

        Button saveBtn = new Button("Save Special Date Hours");
        Button loadBtn = new Button("Load");
        Button deleteBtn = new Button("Delete");

        HBox actions = new HBox(10, saveBtn, loadBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        // ----- Load -----
        loadBtn.setOnAction(e -> {
            if (client == null) { status.setText("❌ No server connection."); return; }
            LocalDate d = datePicker.getValue();
            if (d == null) { status.setText("❌ Please choose a date"); return; }

            ClientSession.activeHandler = (msg) -> {
                if (msg instanceof String s) {
                    Platform.runLater(() -> status.setText(s));
                }
            };

            client.sendRequest(new ClientRequest(
                    ClientRequest.CMD_GET_SPECIAL_OPENING,
                    new Object[]{ d.toString() }
            ));
        });

        // ----- Save -----
        saveBtn.setOnAction(e -> {
            if (client == null) { status.setText("❌ No server connection."); return; }
            LocalDate d = datePicker.getValue();
            if (d == null) { status.setText("❌ Please choose a date"); return; }

            boolean isClosed = closedCheck.isSelected();
            String open = openField.getText().trim();
            String close = closeField.getText().trim();

            if (!isClosed) {
                if (!open.matches("\\d{2}:\\d{2}") || !close.matches("\\d{2}:\\d{2}")) {
                    status.setText("❌ Time format must be HH:MM (e.g., 10:00)");
                    return;
                }
            } else {
                open = null;
                close = null;
            }

            ClientSession.activeHandler = (msg) -> {
                if (msg instanceof String s) {
                    Platform.runLater(() -> status.setText("✅ " + s));
                }
            };

            client.sendRequest(new ClientRequest(
                    ClientRequest.CMD_UPSERT_SPECIAL_OPENING,
                    new Object[]{ d.toString(), open, close, isClosed }
            ));
        });

        // ----- Delete -----
        deleteBtn.setOnAction(e -> {
            if (client == null) { status.setText("❌ No server connection."); return; }
            LocalDate d = datePicker.getValue();
            if (d == null) { status.setText("❌ Please choose a date"); return; }

            ClientSession.activeHandler = (msg) -> {
                if (msg instanceof String s) {
                    Platform.runLater(() -> status.setText("✅ " + s));
                }
            };

            client.sendRequest(new ClientRequest(
                    ClientRequest.CMD_DELETE_SPECIAL_OPENING,
                    new Object[]{ d.toString() }
            ));
        });

        box.getChildren().addAll(
                specialTitle,
                new Label("Special date (optional):"),
                datePicker,
                closedCheck,
                new Label("Open time:"),
                openField,
                new Label("Close time:"),
                closeField,
                actions,
                status
        );

        box.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 8; -fx-background-radius: 8;");

        return box;
    }
    
    @FXML
    private void showOpeningHoursManagement() {
        setSection("Opening Hours");
        if (client == null) {
            setPlaceholder("❌ No server connection.");
            return;
        }

        // מסך ראשי: VBox שמכיל 2 אזורים (Specific + Weekly)
        VBox root = new VBox(15);
        root.setPadding(new Insets(10));

        // ---------------------------
        // A) SPECIAL (ספציפי) - פה את יכולה לקרוא לפונקציה קיימת אצלך
        // ---------------------------
        Label specialTitle = new Label("Special Opening (specific date)");
        specialTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // אם כבר יש לך UI של special (DatePicker, open/close, save וכו') — שימי אותו כאן במקום ה-placeholder
        // לדוגמה: Node specialPane = buildSpecialOpeningPane();
        // בינתיים נשים placeholder כדי שלא יהיה ריק:
        VBox specialBox = buildSpecialOpeningPane();

        specialBox.setPadding(new Insets(10));
        specialBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 8; -fx-background-radius: 8;");

        // ---------------------------
        // B) WEEKLY (רגיל לפי יום)
        // ---------------------------
        Label weeklyTitle = new Label("Weekly Opening Hours (regular days)");
        weeklyTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ListView<String> weeklyList = new ListView<>();
        weeklyList.setPrefHeight(180);

        ComboBox<String> dayBox = new ComboBox<>();
        dayBox.getItems().addAll(
                "SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"
        );
        dayBox.setValue("SUNDAY");

        TextField openField = new TextField();
        openField.setPromptText("Open (HH:mm) e.g. 10:00");

        TextField closeField = new TextField();
        closeField.setPromptText("Close (HH:mm) e.g. 22:00");

        Button refreshBtn = new Button("Refresh");
        Button saveBtn = new Button("Save Day Hours");

        HBox form = new HBox(10, new Label("Day:"), dayBox, openField, closeField, saveBtn, refreshBtn);
        form.setAlignment(Pos.CENTER_LEFT);

        VBox weeklyBox = new VBox(10, weeklyTitle, weeklyList, form);
        weeklyBox.setPadding(new Insets(10));
        weeklyBox.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 8; -fx-background-radius: 8;");

        root.getChildren().addAll(specialBox, weeklyBox);

        contentArea.getChildren().clear();
        contentArea.getChildren().add(root);

        // --- פעולות: להביא שעות שבועיות
        Runnable loadWeekly = () -> {
            ClientSession.activeHandler = (msg) -> {
                if (msg instanceof List<?> list) {
                    Platform.runLater(() -> {
                        weeklyList.getItems().clear();
                        if (list.isEmpty()) {
                            weeklyList.getItems().add("No weekly opening hours found.");
                            return;
                        }
                        if (list.get(0) instanceof String) {
                            @SuppressWarnings("unchecked")
                            List<String> items = (List<String>) list;
                            weeklyList.getItems().addAll(items);
                        } else {
                            weeklyList.getItems().add("⚠️ Unexpected weekly data from server.");
                        }
                    });
                } else if (msg instanceof String s) {
                    Platform.runLater(() -> weeklyList.getItems().add("Server: " + s));
                }
            };

            client.sendRequest(new ClientRequest(ClientRequest.CMD_GET_OPENING_HOURS, new Object[]{}));
        };

        refreshBtn.setOnAction(e -> loadWeekly.run());

        // --- שמירה של יום
        saveBtn.setOnAction(e -> {
            String day = dayBox.getValue();
            String open = openField.getText().trim();
            String close = closeField.getText().trim();

            if (open.isEmpty() || close.isEmpty()) {
                weeklyList.getItems().add("❌ Please enter open & close time.");
                return;
            }

            ClientSession.activeHandler = (msg) -> {
                if (msg instanceof String s) {
                    Platform.runLater(() -> {
                        weeklyList.getItems().add("✅ " + s);
                        loadWeekly.run(); // אחרי שמירה: רענון
                    });
                }
            };

            client.sendRequest(new ClientRequest(
                    ClientRequest.CMD_UPDATE_OPENING_HOURS,
                    new Object[]{day, open, close}
            ));
        });

        // טעינה ראשונית
        loadWeekly.run();
    }



    @FXML
    private void showWaitingList() {
        setSection("Waiting List");

        if (client == null) {
            setPlaceholder("❌ No server connection (client is null).");
            return;
        }

        setPlaceholder("Loading waiting list...");

        ListView<String> listView = new ListView<>();
        contentArea.getChildren().clear();
        contentArea.getChildren().add(listView);

        ClientSession.activeHandler = (msg) -> {
            if (msg instanceof List<?> list) {
                Platform.runLater(() -> {
                    listView.getItems().clear();

                    if (list.isEmpty()) {
                        listView.getItems().add("No one is currently waiting ✅");
                        return;
                    }

                    if (list.get(0) instanceof String) {
                        @SuppressWarnings("unchecked")
                        List<String> items = (List<String>) list;
                        listView.getItems().addAll(items);
                    } else {
                        listView.getItems().add("⚠️ Unexpected data from server.");
                    }
                });
            }
        };

        client.sendRequest(new ClientRequest(ClientRequest.CMD_GET_WAITING_LIST, new Object[]{}));
    }


    @FXML
    private void showOrders() {
        setSection("Orders");
        setPlaceholder("View and manage restaurant orders.");
    }

    @FXML
    private void showReports() {
        setSection("Reports");
        setPlaceholder("View restaurant activity reports.");
    }
    
    @FXML
    private void showVisualReports() {
        setSection("Visual");
        setPlaceholder("View visual restaurant activity reports.");
    }

    @FXML
    private void showCreateSubscriber() {
        setSection("Create Subscriber");
        setPlaceholder("Register a new subscriber and generate a subscription number.");
    }


    // ---------------- LOGOUT ----------------

    @FXML
    private void handleLogout() {
        // for now: just go back to sign-in (same stage)
        // later you can also clear session/user data here
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/clientgui/BistroMain.fxml")
            );
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 700);
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Sign In");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- HELPERS ----------------

    private void setSection(String title) {
        if (sectionTitle != null) sectionTitle.setText(title);
    }

    private void setPlaceholder(String text) {
        contentArea.getChildren().clear();
        Label lbl = new Label(text);
        lbl.getStyleClass().add("placeholder-text");
        contentArea.getChildren().add(lbl);
    }
    
    private void applyRoleUI() {
        boolean isManager = (role == UserRole.MANAGER);

        if (reportsBtn != null) {
            reportsBtn.setVisible(isManager);
            reportsBtn.setManaged(isManager);
        }
    }

}
