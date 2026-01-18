package subscribergui;

import client.ClientController;
import client.ClientSession;
import common.ClientRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * JavaFX controller for subscriber waiting list actions.
 * <p>
 * Allows a logged-in subscriber to join or leave the waiting list. Registers a screen-specific
 * {@link client.ClientSession#activeHandler} to handle server replies and updates the status label accordingly.
 */
public class WaitingListController
{

    /**
     * Text field for entering the number of diners.
     */
    @FXML private TextField txtDiners;

    /**
     * Text field for entering the subscriber's phone (optional, may assist identification).
     */
    @FXML private TextField txtPhone;

    /**
     * Text field for entering the subscriber's email (optional, may assist identification).
     */
    @FXML private TextField txtEmail;

    /**
     * Label used to display status messages and server responses.
     */
    @FXML private Label lblStatus;

    /**
     * Connected client controller used for sending waiting list requests to the server.
     */
    private ClientController client;

    /**
     * The subscriber identifier used to scope join/leave operations.
     */
    private int subscriberId;

    /**
     * Initializes this controller with the active client connection and subscriber context.
     * <p>
     * Resets the status label and registers a screen-specific handler to process server messages
     * for waiting list operations (join/leave, direct table assignment, and closed state).
     *
     * @param client       the connected {@link ClientController} used to communicate with the server
     * @param subscriberId the subscriber identifier associated with this screen
     */
    public void init(ClientController client, int subscriberId)
    {
        this.client = client;
        this.subscriberId = subscriberId;

        if (lblStatus != null)
        {
            lblStatus.setText("");
            lblStatus.setStyle("");
        }

        // handler רק למסך הזה
        ClientSession.activeHandler = (msg)-> {
            if (!(msg instanceof String s)) return;

            System.out.println("CLIENT <- " + s);

            Platform.runLater(()-> {

                // ✅ 1) DIRECT TABLE (הפרוטוקול הנכון)
                if (s.startsWith("WAITING_DIRECT_TABLE|"))
                {
                    // expected: WAITING_DIRECT_TABLE|message|tableNum
                    String[] parts = s.split("\\|", -1);
                    String msgText = (parts.length > 1 && parts[1] != null && !parts[1].isBlank())
                            ? parts[1]
                            : "Added to an empty table. No need to join the waiting list.";
                    String tableStr = (parts.length > 2) ? parts[2].trim() : "?";

                    lblStatus.setText("✅ " + msgText + " (Table " + tableStr + ")");
                    lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                    return;
                }

                // ✅ 2) BACKWARD COMPAT: הפורמט הישן שלך
                if (s.startsWith("Added to an empty table. No need to join the waiting list.|"))
                {
                    String tableStr = extractAfterPipe(s);
                    lblStatus.setText("✅ Added to an empty table. No need to join the waiting list. (Table " + tableStr + ")");
                    lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                    return;
                }

                // ✅ 3) CLOSED
                if (s.equals("WAITING_CLOSED"))
                {
                    lblStatus.setText("Restaurant is closed now.");
                    lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                    return;
                }

                // ✅ 4) JOIN OK
                if (s.startsWith("WAITING_JOIN_OK|"))
                {
                    String code = extractAfterPipe(s);

                    if (isValidWaitingCode(code))
                    {
                        lblStatus.setText("Successfully added to waiting list. Code: " + code);
                    }
                    else
                    {
                        lblStatus.setText("Successfully added to waiting list. (Missing/invalid code: " + code + ")");
                    }
                    lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                    return;
                }

                // ✅ 5) JOIN FAIL
                if (s.startsWith("WAITING_JOIN_FAIL"))
                {
                    String reason = extractAfterPipe(s);
                    lblStatus.setText(reason.isEmpty()
                            ? "Failed to add to waiting list"
                            : "Failed to add to waiting list: " + reason);
                    lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                    return;
                }

                // ✅ 6) LEAVE OK
                if (s.startsWith("WAITING_LEAVE_OK"))
                {
                    lblStatus.setText("Successfully removed from waiting list");
                    lblStatus.setStyle("-fx-text-fill: #1B8F3A; -fx-font-weight: bold;");
                    return;
                }

                // ✅ 7) LEAVE FAIL
                if (s.startsWith("WAITING_LEAVE_FAIL"))
                {
                    String reason = extractAfterPipe(s);
                    lblStatus.setText(reason.isEmpty()
                            ? "Failed to remove from waiting list"
                            : "Failed to remove from waiting list: " + reason);
                    lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                    return;
                }

                // anything else
                lblStatus.setText("Unexpected server reply: " + s);
                lblStatus.setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
            });
        }
        ;
    }

    /**
     * Handles the "Join waiting list" action for a subscriber.
     * <p>
     * Validates the client connection, subscriber context, and diners count, then sends a join request
     * using {@link client.ClientController#joinWaitingListAsSubscriber(int, int, String, String)}.
     */
    @FXML
    private void handleJoin()
    {
        if (client == null)
        {
            showError("Client is not connected");
            return;
        }
        if (subscriberId <= 0)
        {
            showError("Subscriber not set");
            return;
        }

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

        showInfo("Sending join request...");

        String phone = (txtPhone == null || txtPhone.getText() == null) ? "" : txtPhone.getText().trim();
        String email = (txtEmail == null || txtEmail.getText() == null) ? "" : txtEmail.getText().trim();

        client.joinWaitingListAsSubscriber(subscriberId, diners, phone, email);
    }

    /**
     * Handles the "Leave waiting list" action for a subscriber.
     * <p>
     * Validates the client connection and subscriber context, then sends a leave request to the server.
     */
    @FXML
    private void handleLeave()
    {
        if (client == null)
        {
            showError("Client is not connected");
            return;
        }
        if (subscriberId <= 0)
        {
            showError("Subscriber not set");
            return;
        }

        showInfo("Sending leave request...");

        client.sendRequest(new ClientRequest(
                ClientRequest.CMD_LEAVE_WAITING_LIST,
                new Object[] { subscriberId }
        ));
    }

    /**
     * Displays an informational message in the status label using a neutral style.
     *
     * @param msg message to display
     */
    private void showInfo(String msg)
    {
        Platform.runLater(()-> {
            lblStatus.setText(msg);
            lblStatus.setStyle("-fx-text-fill: #666666;");
        });
    }

    /**
     * Displays an error message in the status label using an error style.
     *
     * @param msg message to display
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
     * @param serverMsg the raw server message
     * @return trimmed substring after the first pipe, or an empty string if none exists
     */
    private String extractAfterPipe(String serverMsg)
    {
        int idx = serverMsg.indexOf('|');
        if (idx < 0 || idx + 1 >= serverMsg.length()) return "";
        return serverMsg.substring(idx + 1).trim();
    }

    /**
     * Validates that the waiting list code matches the expected format {@code WLdddddd}.
     *
     * @param code waiting list code string
     * @return {@code true} if the code matches the expected format; otherwise {@code false}
     */
    private boolean isValidWaitingCode(String code)
    {
        if (code == null) return false;
        return code.matches("^WL\\d{6}$");
    }
}
