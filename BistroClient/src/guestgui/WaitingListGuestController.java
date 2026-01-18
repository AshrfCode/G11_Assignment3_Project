package guestgui;

import client.ClientController;
import client.ClientSession;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * JavaFX controller for guest waiting list operations.
 * <p>
 * Supports joining and leaving the waiting list using guest contact details (phone/email).
 * Uses {@link ClientSession#activeHandler} to handle asynchronous server responses for each request.
 */
public class WaitingListGuestController
{

    /**
     * Text field for entering the number of diners to join the waiting list with.
     */
    @FXML private TextField txtDiners;

    /**
     * Text field for entering a contact phone number (optional if email provided).
     */
    @FXML private TextField txtPhone;

    /**
     * Text field for entering a contact email address (optional if phone provided).
     */
    @FXML private TextField txtEmail;

    /**
     * Text field for entering the waiting list code used to leave the waiting list.
     */
    @FXML private TextField txtCode;

    /**
     * Label used to display status messages, confirmations, and errors.
     */
    @FXML private Label lblStatus;

    /**
     * Connected client controller used to communicate with the server.
     */
    private ClientController client;

    /**
     * Initializes this controller with a connected client and resets UI state and active handlers.
     *
     * @param client the connected {@link ClientController}
     */
    public void init(ClientController client)
    {
        this.client = client;

        if (lblStatus != null)
        {
            lblStatus.setText("");
            lblStatus.setStyle("");
        }

        ClientSession.activeHandler = null;
    }

    /**
     * Handles joining the guest waiting list.
     * <p>
     * Validates diners and contact information, installs an active handler for the response,
     * and sends a join request to the server. The server may respond with:
     * <ul>
     *   <li>Direct table assignment (new/old formats)</li>
     *   <li>Restaurant closed</li>
     *   <li>Join OK with waiting code</li>
     *   <li>Join fail with optional reason</li>
     * </ul>
     */
    @FXML
    private void handleJoin()
    {
        if (client == null) { showError("Client is not connected"); return; }

        int diners;
        try
        {
            diners = Integer.parseInt(txtDiners.getText().trim());
            if (diners <= 0) throw new NumberFormatException();
        }
        catch (Exception e)
        {
            showError("Please enter a valid diners number");
            return;
        }

        String phone = (txtPhone == null || txtPhone.getText() == null) ? "" : txtPhone.getText().trim();
        String email = (txtEmail == null || txtEmail.getText() == null) ? "" : txtEmail.getText().trim();

        if (phone.isBlank() && email.isBlank())
        {
            showError("Please enter phone or email");
            return;
        }

        showInfo("Sending join request...");

        // ✅ handler for THIS request only
        ClientSession.activeHandler = (msg)-> {
            if (!(msg instanceof String s)) return;

            Platform.runLater(()-> {
                try
                {
                    System.out.println("CLIENT <- " + s);

                    // ✅ 1) DIRECT TABLE (protocol)
                    if (s.startsWith("WAITING_DIRECT_TABLE|"))
                    {
                        // expected: WAITING_DIRECT_TABLE|message|tableNum
                        String[] parts = s.split("\\|", -1);
                        String msgText = (parts.length > 1 && parts[1] != null && !parts[1].isBlank())
                                ? parts[1]
                                : "Added to an empty table. No need to join the waiting list.";
                        String tableNum = (parts.length > 2) ? parts[2].trim() : "";

                        lblStatus.setText("✅ " + msgText + (tableNum.isEmpty() ? "" : " (Table " + tableNum + ")"));
                        lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                        return;
                    }

                    // ✅ 2) BACKWARD COMPAT: old format you previously sent
                    // "Added to an empty table...|3"
                    if (s.startsWith("Added to an empty table"))
                    {
                        String[] parts = s.split("\\|", -1);
                        String text = parts[0];
                        String tableNum = (parts.length > 1) ? parts[1].trim() : "";

                        lblStatus.setText("✅ " + text + (tableNum.isEmpty() ? "" : " (Table " + tableNum + ")"));
                        lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                        return;
                    }

                    // ✅ 3) CLOSED
                    if (s.equals("WAITING_CLOSED"))
                    {
                        lblStatus.setText("Restaurant is closed right now. Try again during opening hours.");
                        lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                        return;
                    }

                    // ✅ 4) JOIN OK
                    if (s.startsWith("WAITING_GUEST_JOIN_OK|"))
                    {
                        String code = extractAfterPipe(s);
                        lblStatus.setText("Successfully added to waiting list. Code: " + code);
                        lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                        if (txtCode != null) txtCode.setText(code);
                        return;
                    }

                    // ✅ 5) JOIN FAIL
                    if (s.startsWith("WAITING_GUEST_JOIN_FAIL"))
                    {
                        String reason = extractAfterPipe(s);
                        lblStatus.setText("Failed to add: " + (reason.isBlank() ? "DB error" : reason));
                        lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                        return;
                    }

                    // anything else
                    lblStatus.setText("Unexpected server reply: " + s);
                    lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");

                }
                finally
                {
                    ClientSession.activeHandler = null;
                }
            });
        }
        ;

        client.joinWaitingListAsGuest(diners, phone, email);
    }

    /**
     * Handles leaving the guest waiting list using the provided waiting code.
     * <p>
     * Validates the code input, installs an active handler for the response, and sends a leave request.
     * The server may respond with:
     * <ul>
     *   <li>Leave OK</li>
     *   <li>Leave fail with optional reason</li>
     * </ul>
     */
    @FXML
    private void handleLeave()
    {
        if (client == null) { showError("Client is not connected"); return; }

        String code = (txtCode == null || txtCode.getText() == null) ? "" : txtCode.getText().trim();
        if (code.isBlank())
        {
            showError("Please enter your waiting code (WLxxxxxx)");
            return;
        }

        showInfo("Sending leave request...");

        ClientSession.activeHandler = (msg)-> {
            if (!(msg instanceof String s)) return;

            Platform.runLater(()-> {
                try
                {
                    System.out.println("CLIENT <- " + s);

                    if (s.startsWith("WAITING_GUEST_LEAVE_OK"))
                    {
                        lblStatus.setText("Successfully removed from waiting list");
                        lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                        return;
                    }

                    if (s.startsWith("WAITING_GUEST_LEAVE_FAIL"))
                    {
                        String reason = extractAfterPipe(s);
                        lblStatus.setText("Failed to remove: " + (reason.isBlank() ? "Not found" : reason));
                        lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                        return;
                    }

                    lblStatus.setText("Unexpected server reply: " + s);
                    lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");

                }
                finally
                {
                    ClientSession.activeHandler = null;
                }
            });
        }
        ;

        client.leaveWaitingListAsGuest(code);
    }

    /**
     * Displays an informational message in the status label.
     *
     * @param msg message text to display
     */
    private void showInfo(String msg)
    {
        Platform.runLater(()-> {
            lblStatus.setText(msg);
            lblStatus.setStyle("-fx-text-fill: #666666;");
        });
    }

    /**
     * Displays an error message in the status label.
     *
     * @param msg message text to display
     */
    private void showError(String msg)
    {
        Platform.runLater(()-> {
            lblStatus.setText(msg);
            lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
        });
    }

    /**
     * Extracts the substring after the first pipe character ('|') in a server message.
     *
     * @param serverMsg the server message string
     * @return the trimmed substring after the first pipe, or an empty string if not present
     */
    private String extractAfterPipe(String serverMsg)
    {
        int idx = serverMsg.indexOf('|');
        if (idx < 0 || idx + 1 >= serverMsg.length()) return "";
        return serverMsg.substring(idx + 1).trim();
    }
}
