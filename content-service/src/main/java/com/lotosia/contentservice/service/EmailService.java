package com.lotosia.contentservice.service;

import com.lotosia.contentservice.entity.ContactUs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmailAddress;

    @Value("${site.team.notification.email:${spring.mail.username}}")
    private String teamNotificationEmail;

    private EmailService self;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired
    public void setSelf(@Lazy EmailService self) {
        this.self = self;
    }

    @Async
    public void sendSimpleEmail(String recipientEmail, String subject, String text) {

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom(fromEmailAddress);

            mailSender.send(message);

        } catch (MailException e) {

        } catch (Exception e) {

        }
    }

    public void sendContactUsAcknowledgement(String recipientEmail, String userName) {
        String subject = "AgilePulse - Müraciətiniz qəbul edildi";
        String userGreetingName = (userName != null && !userName.isBlank()) ? userName : "Dəyərli İstifadəçi";

        String text = String.format(
                """
                        Salam %s,
                        
                        "Bizimlə Əlaqə" vasitəsilə göndərdiyiniz mesaj qəbul edildi.
                        Ən qısa zamanda sizinlə əlaqə saxlanılacaqdır.
                        
                        Bizimlə əlaqə saxladığınız üçün təşəkkür edirik!
                        
                        Hörmətlə,
                        Lotosia Komandası
                        """,
                userGreetingName
        );

        self.sendSimpleEmail(recipientEmail, subject, text);
    }

    public void sendNewContactUsNotificationToTeam(ContactUs contactUsEntity) {
        String subject = String.format("Yeni 'Bizimlə Əlaqə' Müraciəti Alındı - ID: %d", contactUsEntity.getId());

        String formattedTimestamp = contactUsEntity.getCreatedAt() != null ?
                contactUsEntity.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A";

        String text = String.format(
                """
                        Yeni bir "Bizimlə Əlaqə" müraciəti daxil oldu:
                        
                        Müraciət ID: %d
                        Ad: %s
                        Soyad: %s
                        Email: %s
                        Göndərilmə Tarixi: %s
                        
                        Mesaj:
                        --------------------
                        %s
                        --------------------
                        """,
                contactUsEntity.getId(),
                contactUsEntity.getFirstName(),
                contactUsEntity.getLastName(),
                contactUsEntity.getEmail(),
                formattedTimestamp,
                contactUsEntity.getMessage()
        );

        self.sendSimpleEmail(teamNotificationEmail, subject, text);
    }
}
