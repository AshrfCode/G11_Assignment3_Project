package representativegui;

import client.ClientManager;
import client.ClientSession;
import common.ClientRequest;
import entities.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

public class ManageSubscribersController {

    // =========================
    // TABLE
    // =========================

    @FXML private TableView<User> subscribersTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, Boolean> colActive;
    @FXML private TableColumn<User, Timestamp> colCreatedAt;

    // =========================
    // FORM
    // =========================

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox activeCheck;

    @FXML private Label msgLabel;

    // =========================
    // INITIALIZE
    // =========================

    @FXML
    private void initialize() {

        ClientSession.activeHandler = this::handleServerMessage;

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // כשבוחרים מנוי – למלא שדות
        subscribersTable.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            if (sel != null) {
                nameField.setText(sel.getName());
                emailField.setText(sel.getEmail());
                phoneField.setText(sel.getPhone());
                activeCheck.setSelected(sel.isActive());
                passwordField.clear(); // לא מציגים סיסמה
            }
        });

        loadSubscribers();
    }

    // =========================
    // SERVER HANDLER
    // =========================

    private void handleServerMessage(Object msg) {

        // ✅ רשימת מנויים
        if (msg instanceof List<?> list && (list.isEmpty() || list.get(0) instanceof User)) {

            @SuppressWarnings("unchecked")
            List<User> users = (List<User>) list;

            Platform.runLater(() -> {
                subscribersTable.setItems(FXCollections.observableArrayList(users));
                msgLabel.setText("Loaded " + users.size() + " subscribers.");
            });
            return;
        }

        // ✅ הודעת טקסט
        if (msg instanceof String s) {
            Platform.runLater(() -> msgLabel.setText(s));
        }
    }

    // =========================
    // LOAD
    // =========================

    private void loadSubscribers() {
        try {
            ClientManager.getClient().sendToServer(
                new ClientRequest(ClientRequest.CMD_GET_ALL_SUBSCRIBERS, new Object[0])
            );
        } catch (IOException e) {
            e.printStackTrace();
            msgLabel.setText("❌ Failed to load subscribers");
        }
    }

    // =========================
    // ACTIONS
    // =========================

    @FXML
    private void refresh() {
        loadSubscribers();
    }

    @FXML
    private void addSubscriber() {

        try {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String password = passwordField.getText();
            boolean active = activeCheck.isSelected();

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                msgLabel.setText("❌ Fill required fields");
                return;
            }

            Object[] params = { name, email, phone, password, active };

            ClientManager.getClient().sendToServer(
                new ClientRequest("ADD_SUBSCRIBER", params)
            );

        } catch (Exception e) {
            msgLabel.setText("❌ Invalid input");
        }
    }

    @FXML
    private void updateSubscriber() {

        User selected = subscribersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            msgLabel.setText("❌ Select subscriber first");
            return;
        }

        try {
            selected.setName(nameField.getText().trim());
            selected.setEmail(emailField.getText().trim());
            selected.setPhone(phoneField.getText().trim());
            selected.setActive(activeCheck.isSelected());

            ClientManager.getClient().sendToServer(
                new ClientRequest("UPDATE_SUBSCRIBER", new Object[]{ selected })
            );

        } catch (Exception e) {
            msgLabel.setText("❌ Update failed");
        }
    }
    
    @FXML
    private void openAddSubscriber() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/representativegui/AddSubscriber.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add Subscriber");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadSubscribers(); // refresh table

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void deactivateSubscriber() {

        User selected = subscribersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            msgLabel.setText("❌ Select subscriber first");
            return;
        }

        try {
            ClientManager.getClient().sendToServer(
                new ClientRequest("DEACTIVATE_SUBSCRIBER",
                    new Object[]{ selected.getId() })
            );
        } catch (IOException e) {
            e.printStackTrace();
            msgLabel.setText("❌ Action failed");
        }
    }
}
