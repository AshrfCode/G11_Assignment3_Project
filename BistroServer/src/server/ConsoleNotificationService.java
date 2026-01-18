package server;

/**
 * A simple {@link NotificationService} implementation that writes outgoing
 * notifications to the server console instead of sending real messages.
 * <p>
 * Useful for local development, debugging, and testing without email/SMS providers.
 */
public class ConsoleNotificationService implements NotificationService {

    /**
     * "Sends" an email by printing its details to the console.
     *
     * @param toEmail  recipient email address
     * @param subject  email subject line
     * @param body     email body content
     */
    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        System.out.println("📧 [EMAIL] To: " + toEmail);
        System.out.println("📧 [EMAIL] Subject: " + subject);
        System.out.println("📧 [EMAIL] Body: " + body);
    }

    /**
     * "Sends" an SMS by printing its details to the console.
     *
     * @param toPhone recipient phone number
     * @param body    SMS message content
     */
    @Override
    public void sendSms(String toPhone, String body) {
        System.out.println("📱 [SMS] To: " + toPhone);
        System.out.println("📱 [SMS] Body: " + body);
    }
}
