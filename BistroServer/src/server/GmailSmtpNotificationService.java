package server;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class GmailSmtpNotificationService implements NotificationService {

    private final Session session;
    private final String fromEmail;

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

    @Override
    public void sendSms(String toPhone, String body) {
        // Gmail cannot send real SMS -> mock for now
        System.out.println("📱 [SMS MOCK] To: " + toPhone + " | " + body);
    }
}