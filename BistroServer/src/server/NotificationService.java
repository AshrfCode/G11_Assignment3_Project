package server;

/**
 * Defines a notification service capable of sending messages through different channels,
 * such as email and SMS.
 */
public interface NotificationService {

    /**
     * Sends an email message to the specified recipient.
     *
     * @param toEmail  the recipient email address
     * @param subject  the email subject line
     * @param body     the email message body
     */
    void sendEmail(String toEmail, String subject, String body);

    /**
     * Sends an SMS message to the specified phone number.
     *
     * @param toPhone  the destination phone number
     * @param body     the SMS message body
     */
    void sendSms(String toPhone, String body);
}
