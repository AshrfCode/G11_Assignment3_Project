package server;

public interface NotificationService {
    void sendEmail(String toEmail, String subject, String body);
    void sendSms(String toPhone, String body);
}