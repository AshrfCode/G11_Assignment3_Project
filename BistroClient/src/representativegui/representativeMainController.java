package representativegui;

import java.time.LocalDate;
import java.util.List;
import common.UserRole;
import client.ClientController;
import client.ClientSession;
import common.ClientRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;


/**
 * JavaFX main controller for the representative/manager dashboard.
 * <p>
 * Loads different management screens (subscribers, tables, waiting list, visual reports,
 * opening hours, and today's reservations) into the {@link #contentArea}.
 * <p>
 * Relies on {@link ClientSession#activeHandler} for asynchronous server responses and
 * uses {@link ClientController} to send requests to the server.
 */
public class representativeMainController {
	
	/**
     * Connected client controller used to communicate with the server.
     */
	private ClientController client;

    /**
     * Sets the connected client and loads the default screen once a connection is available.
     *
     * @param client the connected {@link ClientController}
     */
    public void setClient(ClientController client) {
        this.client = client;
        
        if (this.client != null) {
        	showReservationsToday(); // Load the default screen NOW, because we have a connection.
        }
    }


    /**
     * Entry mode for the representative UI.
     */
    public enum EntryMode {
        HOME,
        RESTAURANT
    }

    /**
     * Current logged-in role for this controller; used to toggle UI features.
     */
    private UserRole role = UserRole.REPRESENTATIVE;

    /**
     * Label showing the currently selected section title.
     */
    @FXML private Label sectionTitle;

    /**
     * Label showing role and mode information.
     */
    @FXML private Label modeBadge;

    /**
     * Label used to greet the current user.
     */
    @FXML private Label heyUserLabel;

    /**
     * Main content area where views are loaded dynamically.
     */
    @FXML private StackPane contentArea;

    /**
     * Button that navigates to the waiting list view (visible in restaurant mode).
     */
    @FXML private Button waitingListBtn;

    /**
     * Button that navigates to reports (visible for managers).
     */
    @FXML private Button reportsBtn;

    /**
     * Current entry mode.
     */
    private EntryMode entryMode = EntryMode.HOME;

    /**
     * Display name used in the greeting label.
     */
    private String username = "Subscriber";

    // =========================
    // INITIALIZE
    // =========================

    /**
     * JavaFX initialization hook.
     * <p>
     * Applies UI visibility rules for role/mode and initializes the greeting label.
     */
    @FXML
    public void initialize() {
        applyModeUI();
        applyRoleUI();
        setUsername(username);
    }
    

    // =========================
    // NAVIGATION – LOAD INTO contentArea
    // =========================

    /**
     * Loads the manage subscribers view into the content area.
     */
    @FXML
    private void showManageSubscribers() {
        loadContent("/representativegui/ManageSubscribers.fxml",
                "Manage Subscribers");
    }

    /**
     * Loads the manage tables view into the content area.
     */
    @FXML
    private void showManageTables() {
        loadContent("/representativegui/ManageTables.fxml",
                "Manage Tables");
    }
  
    // ---------------- NAV ACTIONS ----------------
    /**
     * Loads the orders management view into the content area and triggers an initial data refresh.
     */
    @FXML
	private void showReservations() {
	    setSection("Manage Orders");
	
	    if (client == null) {
	        setPlaceholder("❌ No server connection.");
	        return;
	    }
	
	    try {
	        // 1. Load the new FXML
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/representativegui/ManageOrdersView.fxml"));
	        Parent root = loader.load();
	
	        // 2. Get controller and pass client
	        ManageOrdersController controller = loader.getController();
	        controller.setClient(this.client);
	        
	        // 3. Trigger initial data load
	        controller.start();
	
	        // 4. Show it
	        contentArea.getChildren().clear();
	        contentArea.getChildren().add(root);
	
	    } catch (Exception e) {
	        e.printStackTrace();
	        setPlaceholder("Error loading orders view.");
	    }
	}	

    
    /**
     * Builds a UI pane for managing special opening hours for a specific date.
     * <p>
     * Provides actions to load, save (upsert), and delete special opening hours for a chosen date.
     *
     * @return a {@link VBox} containing the special opening hours controls
     */
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

        specialTitle.getStyleClass().add("section-title");
        box.getStyleClass().add("section-card");
        status.getStyleClass().add("opening-status");

        saveBtn.getStyleClass().add("mint-btn");
        loadBtn.getStyleClass().add("mint-btn");
        deleteBtn.getStyleClass().add("peach-btn");

        return box;
    }
    
    /**
     * Displays the opening hours management screen.
     * <p>
     * Shows two sections:
     * <ul>
     *   <li>Special opening hours for a specific date</li>
     *   <li>Weekly opening hours per day</li>
     * </ul>
     * Sends server requests to load and update opening hours and displays responses in the UI.
     */
    @FXML
    private void showOpeningHoursManagement() {
        setSection("Opening Hours");
        if (client == null) {
            setPlaceholder("❌ No server connection.");
            return;
        }

        // מסך ראשי: VBox שמכיל 2 אזורים (Specific + Weekly)
        VBox root = new VBox(15);
        root.getStylesheets().add(
        	    getClass().getResource("/representativegui/OpeningHoursView.css").toExternalForm()
        	);
        	root.getStyleClass().add("opening-root");

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
    
        // ---------------------------
        // B) WEEKLY (רגיל לפי יום)
        // ---------------------------
        Label weeklyTitle = new Label("Weekly Opening Hours (regular days)");
        weeklyTitle.getStyleClass().add("section-title");
        

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
        weeklyBox.getStyleClass().add("section-card");

        saveBtn.getStyleClass().add("mint-btn");
        refreshBtn.getStyleClass().add("mint-btn");
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


    /**
     * Loads and displays the waiting list management view.
     * <p>
     * Injects the current {@link ClientController} into the waiting list controller and triggers
     * an initial data load.
     */
    @FXML
    private void showWaitingList() {
        setSection("Waiting List");

        if (client == null) {
            setPlaceholder("❌ No server connection.");
            return;
        }

        try {
            // 1. Load the new clean FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/representativegui/WaitingListView.fxml"));
            Parent root = loader.load();

            // 2. Get controller and pass client
            WaitingListController controller = loader.getController();
            controller.setClient(this.client);
            
            // 3. Trigger initial data load
            controller.start();

            // 4. Show it
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (Exception e) {
            e.printStackTrace();
            setPlaceholder("Error loading waiting list view.");
        }
    }


    /**
     * Displays a placeholder for an orders section.
     */
    @FXML
    private void showOrders() {
        setSection("Orders");
        setPlaceholder("View and manage restaurant orders.");
    }

    /**
     * Displays a placeholder for a reports section.
     */
    @FXML
    private void showReports() {
        setSection("Reports");
        setPlaceholder("View restaurant activity reports.");
    }

    /**
     * Loads and displays the visual reports menu.
     * <p>
     * Injects the current {@link ClientController} and {@link #contentArea} into the menu controller
     * so that charts can be loaded into the main view.
     */
    @FXML
    private void showVisualReports() {
        setSection("Visual Reports");

        if (client == null) {
            setPlaceholder("❌ No server connection.");
            return;
        }

        try {
            // 1. Load the Menu FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/representativegui/VisualReportsMenu.fxml"));
            Parent root = loader.load();

            // 2. Get the controller
            VisualReportsMenuController menuController = loader.getController();
            
            // 3. Pass dependencies
            menuController.setClient(this.client);
            menuController.setMainContentArea(this.contentArea); // So the menu can load charts into the main view

            // 4. Show the menu
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (Exception e) {
            e.printStackTrace();
            setPlaceholder("Error loading reports menu.");
        }
    }

    /**
     * Displays a placeholder for creating a subscriber.
     */
    @FXML
    private void showCreateSubscriber() {
        setSection("Create Subscriber");
        setPlaceholder("Register a new subscriber.");
    }

    /**
     * Loads and displays today's reservations view.
     * <p>
     * Installs a refresh action that requests today's reservations from the server and passes
     * the received list to the {@code TodaysReservationsController} for parsing and display.
     */
    @FXML
    private void showReservationsToday() {
        setSection("Today's Reservations");

        if (client == null) {
            setPlaceholder("❌ No server connection.");
            return;
        }

        try {
            // 1. Load the new FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/representativegui/TodaysReservations.fxml"));
            Parent root = loader.load();
            
            // 2. Get the controller so we can pass data to it later
            TodaysReservationsController tableController = loader.getController();

            // 3. Define what the refresh button does
            Runnable fetchAction = () -> {
                ClientSession.activeHandler = (msg) -> {
                    if (msg instanceof List<?> list) {
                        Platform.runLater(() -> {
                            if (!list.isEmpty() && list.get(0) instanceof String) {
                                 // Pass the raw strings to the table controller to parse
                                tableController.updateTableData((List<String>) list);
                            } else {
                                // Handle empty or weird data
                                tableController.updateTableData(List.of("No reservations today"));
                            }
                        });
                    }
                };
                // Send the request
                client.sendRequest(new ClientRequest(ClientRequest.CMD_GET_TODAY_RESERVATIONS, new Object[]{}));
            };

            // 4. Set the refresh action and trigger it immediately
            tableController.setRefreshAction(fetchAction);
            fetchAction.run(); // Load data now

            // 5. Show the view
            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

        } catch (Exception e) {
            e.printStackTrace();
            setPlaceholder("Error loading reservations view.");
        }
    }

    // =========================
    // LOGOUT
    // =========================

    /**
     * Logs out the current user and navigates back to the sign-in screen.
     * <p>
     * Clears {@link ClientSession} state and loads {@code BistroMain.fxml}.
     */
    @FXML
    private void handleLogout() {
        try {
        	ClientSession.clear();
        	
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/clientgui/BistroMain.fxml")
            );
            Parent root = loader.load();

            javafx.stage.Stage stage =
                    (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, 1000, 700));
            stage.centerOnScreen();
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.setTitle("Bistro – Sign In");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // HELPERS
    // =========================

    /**
     * Loads an FXML view into the content area and updates the current section title.
     *
     * @param fxmlPath path to the FXML resource
     * @param title    section title to display
     */
    private void loadContent(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);
            setSection(title);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the section title label.
     *
     * @param title section title text
     */
    private void setSection(String title) {
        if (sectionTitle != null) {
            sectionTitle.setText(title);
        }
    }
    
    /**
     * Sets the entry mode (HOME/RESTAURANT) and applies related UI rules.
     *
     * @param mode the entry mode to apply
     */
    public void setEntryMode(EntryMode mode) {
        if (mode != null) {
            this.entryMode = mode;
        }
        applyModeUI();
    }


    /**
     * Shows a simple placeholder label in the content area.
     *
     * @param text placeholder text to display
     */
    private void setPlaceholder(String text) {
        contentArea.getChildren().clear();
        Label lbl = new Label(text);
        lbl.getStyleClass().add("placeholder-text");
        contentArea.getChildren().add(lbl);
    }

    /**
     * Sets the displayed username for the greeting label.
     *
     * @param username the username to display
     */
    public void setUsername(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.username = username.trim();
        }
        heyUserLabel.setText("Hey " + this.username);
    }

    /**
     * Sets the current role and applies role-based and mode-based UI visibility rules.
     *
     * @param role the current {@link UserRole}
     */
    public void setRole(UserRole role) {
        if (role != null) {
            this.role = role;
        }
        applyRoleUI();
        applyModeUI();
    }

    /**
     * Applies UI changes based on the current {@link #entryMode} and {@link #role}.
     * <p>
     * Updates the mode badge text and controls visibility of restaurant-only actions.
     */
    private void applyModeUI() {
        boolean isRestaurant = (entryMode == EntryMode.RESTAURANT);

        String roleText = (role != null) ? role.name() : "UNKNOWN";
        String modeText = isRestaurant ? "Restaurant Mode" : "Home Mode";

        if (modeBadge != null) {
            modeBadge.setText(roleText + " | " + modeText);
        }

        if (waitingListBtn != null) {
            waitingListBtn.setVisible(isRestaurant);
            waitingListBtn.setManaged(isRestaurant);
        }
    }

    /**
     * Applies UI changes based on the current {@link #role}.
     * <p>
     * Shows reports-related actions only for managers.
     */
    private void applyRoleUI() {
        boolean isManager = (role == UserRole.MANAGER);

        if (reportsBtn != null) {
            reportsBtn.setVisible(isManager);
            reportsBtn.setManaged(isManager);
        }
    }
}
