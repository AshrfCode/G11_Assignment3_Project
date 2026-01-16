package server;

public class ConsoleNotificationService implements NotificationService {

    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        System.out.println("📧 [EMAIL] To: " + toEmail);
        System.out.println("📧 [EMAIL] Subject: " + subject);
        System.out.println("📧 [EMAIL] Body: " + body);
    }

    @Override
    public void sendSms(String toPhone, String body) {
        System.out.println("📱 [SMS] To: " + toPhone);
        System.out.println("📱 [SMS] Body: " + body);
    }
}
