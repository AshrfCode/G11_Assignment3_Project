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


/**
 * JavaFX controller for creating reservations as a guest or subscriber.
 * <p>
 * Supports:
 * <ul>
 *   <li>Checking available reservation time slots for a selected date and party size</li>
 *   <li>Submitting a reservation request for a chosen date/time</li>
 *   <li>Subscriber mode with auto-filled/locked contact details</li>
 * </ul>
 * Uses {@link ClientSession#activeHandler} to receive asynchronous responses from the server.
 */
public class ReservationController {

    /**
     * Entry mode for this screen (HOME vs RESTAURANT), retained for future behavior differences.
     */
    public enum EntryMode { HOME, RESTAURANT }

    /**
     * Maximum number of diners allowed for a single reservation request.
     */
    private static final int MAX_DINERS = 10;

    /**
     * Date picker for selecting the reservation date.
     */
    @FXML private DatePicker datePicker;

    /**
     * Combo box for selecting or typing a reservation time (HH:mm).
     */
    @FXML private ComboBox<String> timeCombo;

    /**
     * Text field for entering the number of diners.
     */
    @FXML private TextField dinersField;

    /**
     * Text field for entering the contact phone number (guest mode).
     */
    @FXML private TextField phoneField;

    /**
     * Text field for entering the contact email address (guest mode).
     */
    @FXML private TextField emailField;

    /**
     * Label used to display status messages and validation/errors to the user.
     */
    @FXML private Label statusLabel;

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Indicates whether this controller is operating in subscriber mode.
     */
    // Subscriber mode
    private boolean subscriberMode = false;

    /**
     * Subscriber ID used to associate reservations with a subscriber account.
     */
    private int subscriberId = -1;

    /**
     * Reserved for a possible timeout mechanism in the reservation flow.
     */
    private PauseTransition reserveTimeout;

    /**
     * Flag indicating whether a reservation reply is currently being awaited.
     */
    private boolean waitingReserveReply = false;

    /**
     * Indicates whether a reservation reply has arrived (used for fallback behavior).
     */
    private volatile boolean reserveReplyArrived = false;


    /**
     * Current entry mode for the screen (HOME/RESTAURANT).
     */
    // Mode (Home / Restaurant) - kept if you need it later
    private EntryMode entryMode = EntryMode.HOME;

    /**
     * Sets the client controller used for server communication.
     *
     * @param client the connected {@link ClientController}
     */
    public void setClient(ClientController client) {
        this.client = client;
    }

    /**
     * Sets the entry mode for this screen.
     *
     * @param mode the desired {@link EntryMode}; defaults to {@link EntryMode#HOME} if null
     */
    public void setEntryMode(EntryMode mode) {
        this.entryMode = (mode == null) ? EntryMode.HOME : mode;
    }

    /**
     * Enables subscriber mode and auto-fills/locks the email and phone fields.
     *
     * @param id    subscriber ID
     * @param email subscriber email (may be null)
     * @param phone subscriber phone (may be null)
     */
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

    /**
     * JavaFX initialization hook.
     * <p>
     * Initializes default diners value, enables editable time input, and shows initial guidance.
     */
    @FXML
    public void initialize() {
        dinersField.setText("2");
        timeCombo.setEditable(true);
        setStatus("Pick date + diners, then type a time (HH:mm) or click Check availability.");
    }

    /**
     * Handles checking availability for a given date and diners count.
     * <p>
     * Validates inputs and constraints (date range, diners range, optional typed time format),
     * registers an active handler to receive a list of available slots, and requests slots from
     * the server.
     */
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
                    
                    if (list.get(0).equals("CLOSED")) {
                        timeCombo.getItems().clear();
                        setStatus("❌ The restaurant is closed on this date.");
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

    /**
     * Handles submitting a reservation request.
     * <p>
     * Validates date/time/diners and contact details, registers an active handler to process the
     * server response, and sends the reservation creation request. If the reservation is full,
     * triggers a slots reload flow to help the user pick another time.
     */
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



    /**
     * Requests available slots from the server and opens the time dropdown when the previously
     * requested time is not available.
     * <p>
     * Updates the time list, clears the invalid time selection, and prompts the user to choose
     * an available option.
     *
     * @param dateStr        reservation date string (typically {@code YYYY-MM-DD})
     * @param diners         number of diners (party size)
     * @param requestedTime  the time that was requested but found unavailable
     */
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



    /**
     * Parses a positive integer from the provided string.
     *
     * @param s input string to parse
     * @return parsed positive integer, or {@code -1} if parsing fails or value is not positive
     */
    private int parsePositiveInt(String s) {
        try {
            int x = Integer.parseInt(s.trim());
            return x > 0 ? x : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Updates the status label text if available.
     *
     * @param msg the message to display
     */
    private void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }
    
}
