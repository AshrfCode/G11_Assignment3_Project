package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;


public class ReservationController {

    public enum EntryMode { HOME, RESTAURANT }

    private static final int MAX_DINERS = 10;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeCombo;
    @FXML private TextField dinersField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    private ClientController client;

    // Subscriber mode
    private boolean subscriberMode = false;
    private int subscriberId = -1;
    private PauseTransition reserveTimeout;
    private boolean waitingReserveReply = false;
    private volatile boolean reserveReplyArrived = false;


    // Mode (Home / Restaurant) - kept if you need it later
    private EntryMode entryMode = EntryMode.HOME;

    public void setClient(ClientController client) {
        this.client = client;
    }

    public void setEntryMode(EntryMode mode) {
        this.entryMode = (mode == null) ? EntryMode.HOME : mode;
    }

    public void setSubscriberInfo(int id, String email, String phone) {
        subscriberMode = true;
        subscriberId = id;

        if (emailField != null) {
            emailField.setText(email == null ? "" : email.trim());
            emailField.setDisable(true);
        }
        if (phoneField != null) {
            phoneField.setText(phone == null ? "" : phone.trim());
            phoneField.setDisable(true);
        }
    }

    @FXML
    public void initialize() {
        dinersField.setText("2");
        timeCombo.setEditable(true);
        setStatus("Pick date + diners, then type a time (HH:mm) or click Check availability.");
    }

    @FXML
    private void handleCheckAvailability() {
        if (client == null) {
            setStatus("❌ No client connection.");
            return;
        }

        LocalDate date = datePicker.getValue();
        if (date == null) {
            setStatus("❌ Please choose a date.");
            return;
        }

        int diners = parsePositiveInt(dinersField.getText());
        if (diners <= 0) {
            setStatus("❌ Diners must be a positive number.");
            return;
        }
        if (diners > MAX_DINERS) {
            setStatus("❌ You can't book more than " + MAX_DINERS + " people.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime min = now.plusHours(1);
        LocalDateTime max = now.plusMonths(1);
        if (date.isBefore(min.toLocalDate()) || date.isAfter(max.toLocalDate())) {
            setStatus("❌ Date must be from today+1h up to 1 month ahead.");
            return;
        }

        // typed/selected time
        String chosenTime = timeCombo.getValue();
        if (chosenTime == null || chosenTime.isEmpty()) {
            chosenTime = timeCombo.getEditor() != null ? timeCombo.getEditor().getText().trim() : "";
        }

        // validate typed time if exists
        if (chosenTime != null && !chosenTime.isEmpty()) {
            try {
                LocalTime t = LocalTime.parse(chosenTime);
                LocalDateTime requested = LocalDateTime.of(date, t);

                if (requested.isBefore(LocalDateTime.now().plusHours(1))) {
                    setStatus("❌ Reservation must be at least 1 hour from now. Choose a later time.");
                    return;
                }
                if (requested.isAfter(LocalDateTime.now().plusMonths(1))) {
                    setStatus("❌ Reservation can be made up to 1 month ahead. Choose an earlier date/time.");
                    return;
                }
            } catch (Exception ex) {
                setStatus("❌ Time format must be HH:mm (example: 19:30).");
                return;
            }
        }

        final String timeForMsg = chosenTime;
        setStatus("Checking availability...");

        ClientSession.activeHandler = (msg) -> {
            if (msg instanceof List<?> list) {
                Platform.runLater(() -> {

                    // Fill items
                    if (list.isEmpty()) {
                        timeCombo.getItems().clear();
                        setStatus("❌ No available slots for this date.");
                        return;
                    }

                    if (!(list.get(0) instanceof String)) {
                        return;
                    }

                    @SuppressWarnings("unchecked")
                    List<String> slots = (List<String>) list;

                    timeCombo.getItems().setAll(slots);

                    // If user typed a time and it's NOT available -> do your "full -> load -> open" behavior
                    if (timeForMsg != null && !timeForMsg.isEmpty()) {
                        boolean ok = slots.contains(timeForMsg);

                        if (ok) {
                            timeCombo.setValue(timeForMsg);
                            if (timeCombo.getEditor() != null) timeCombo.getEditor().setText(timeForMsg);
                            setStatus("✅ Available at " + timeForMsg + ".");
                        } else {
                            // Clear wrong time and OPEN the dropdown
                            timeCombo.setValue("");
                            if (timeCombo.getEditor() != null) timeCombo.getEditor().setText("");

                            setStatus("❌ Full at " + timeForMsg + ". Loading available times…");

                            // show the list and then message
                            timeCombo.show();
                            setStatus("Please choose an available time from the list.");
                        }
                    } else {
                        // No typed time, just load + open
                        setStatus("✅ Available times loaded.");
                        timeCombo.show();
                    }
                });
            }
        };

        client.requestAvailableSlots(date.toString(), diners);
    }

    @FXML
    private void handleReserve() {
        if (client == null) {
            setStatus("❌ No client connection.");
            return;
        }

        LocalDate date = datePicker.getValue();
        if (date == null) {
            setStatus("❌ Please choose a date.");
            return;
        }

        String time = timeCombo.getValue();
        if (time == null || time.isEmpty()) {
            time = (timeCombo.getEditor() != null) ? timeCombo.getEditor().getText().trim() : "";
        }
        if (time.isEmpty()) {
            setStatus("❌ Please choose or type a time (HH:mm).");
            return;
        }

        int diners = parsePositiveInt(dinersField.getText());
        if (diners <= 0) {
            setStatus("❌ Diners must be a positive number.");
            return;
        }
        if (diners > MAX_DINERS) {
            setStatus("❌ You can't book more than " + MAX_DINERS + " people.");
            return;
        }

        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();

        if (!subscriberMode && phone.isEmpty() && email.isEmpty()) {
            setStatus("❌ Please enter phone or email.");
            return;
        }

        LocalDateTime start;
        try {
            start = LocalDateTime.of(date, LocalTime.parse(time));
        } catch (Exception e) {
            setStatus("❌ Time format must be HH:mm (example: 19:30).");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now.plusHours(1)) || start.isAfter(now.plusMonths(1))) {
            setStatus("❌ Reservation must be at least 1 hour from now and up to 1 month ahead.");
            return;
        }

        String dateTimeStr = date + " " + time;

        setStatus("Sending reservation...");
        reserveReplyArrived = false;

        final int dinersFinal = diners;
        final String timeFinal = time;
        final String dateStrFinal = date.toString();

        // ✅ FALLBACK: if no server reply arrives, don't get stuck
        PauseTransition fallback = new PauseTransition(Duration.seconds(2.5));
        fallback.setOnFinished(e -> {
            if (!reserveReplyArrived) {
                Platform.runLater(() -> setStatus("❌ Full at " + timeFinal + ". Loading available times..."));
                loadSlotsAndOpen(dateStrFinal, dinersFinal, timeFinal);
            }
        });
        fallback.play();

        ClientSession.activeHandler = (msg) -> {
            if (msg instanceof String s) {
                reserveReplyArrived = true;
                fallback.stop();

                if (s.startsWith("RESERVATION_OK|")) {
                    Platform.runLater(() -> {
                        String code = s.substring("RESERVATION_OK|".length());
                        setStatus("✅ Reserved! Confirmation code: " + code);
                    });
                    return;
                }

                // full or fail -> load times
                if (s.startsWith("RESERVATION_FAIL") || s.startsWith("RESERVATION_FULL")) {
                    Platform.runLater(() -> setStatus("❌ Full at " + timeFinal + ". Loading available times..."));
                    loadSlotsAndOpen(dateStrFinal, dinersFinal, timeFinal);
                }
            }
        };

        String customerKey = (subscriberMode && subscriberId > 0)
                ? "SUB:" + subscriberId
                : (!email.isEmpty() ? email : phone);

        String chosenEmail = (!email.isEmpty()) ? email : "";

        // you can keep this direct call:
        client.createReservation(dateTimeStr, diners, customerKey, phone, chosenEmail);
    }



    // ✅ One helper used by Reserve-full behavior
    private void loadSlotsAndOpen(String dateStr, int diners, String requestedTime) {

        ClientSession.activeHandler = (msg) -> {
            if (msg instanceof List<?> list) {
                if (list.isEmpty()) {
                    Platform.runLater(() -> setStatus("❌ No available slots for this date."));
                    return;
                }

                if (list.get(0) instanceof String) {
                    @SuppressWarnings("unchecked")
                    List<String> slots = (List<String>) list;

                    Platform.runLater(() -> {
                        timeCombo.getItems().setAll(slots);

                        // clear invalid time
                        timeCombo.setValue("");
                        if (timeCombo.getEditor() != null) timeCombo.getEditor().setText("");

                        setStatus("❌ Full at " + requestedTime + ". Please choose an available time from the list.");

                        // open dropdown
                        timeCombo.show();
                    });
                }
            }
        };

        client.requestAvailableSlots(dateStr, diners);
    }



    private int parsePositiveInt(String s) {
        try {
            int x = Integer.parseInt(s.trim());
            return x > 0 ? x : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
    
}