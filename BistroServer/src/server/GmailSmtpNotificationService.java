package server;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Notification service implementation that sends email notifications via Gmail's SMTP server.
 * <p>
 * This service uses an application-specific password for authentication and supports sending
 * plain-text email messages. SMS sending is not supported by Gmail SMTP and is therefore mocked.
 * </p>
 */
public class GmailSmtpNotificationService implements NotificationService {

    /**
     * Pre-configured Jakarta Mail session used to create and send messages through Gmail SMTP.
     */
    private final Session session;

    /**
     * The sender email address used as the "From" address for outgoing emails.
     */
    private final String fromEmail;

    /**
     * Constructs a Gmail SMTP notification service using the provided sender email and Gmail app password.
     *
     * @param fromEmail    the sender email address to use as the "From" address
     * @param appPassword  the Gmail application password used for SMTP authentication
     */
    public GmailSmtpNotificationService(String fromEmail, String appPassword) {
        this.fromEmail = fromEmail;

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });
    }

    /**
     * Sends a plain-text email message to the specified recipient using Gmail SMTP.
     *
     * @param toEmail  the recipient email address
     * @param subject  the email subject line
     * @param body     the plain-text email body
     */
    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            msg.setSubject(subject);
            msg.setText(body);

            Transport.send(msg);
            System.out.println("📧 Sent email to: " + toEmail);

        } catch (Exception e) {
            System.err.println("📧 Email failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends an SMS message to the specified phone number.
     * <p>
     * Gmail SMTP cannot send real SMS messages, so this method provides a mock implementation
     * that logs the message to the console.
     * </p>
     *
     * @param toPhone  the destination phone number
     * @param body     the SMS message body
     */
    @Override
    public void sendSms(String toPhone, String body) {
        // Gmail cannot send real SMS -> mock for now
        System.out.println("📱 [SMS MOCK] To: " + toPhone + " | " + body);
    }
}
