package com.lotosia.supportservice.service;

import com.lotosia.supportservice.entity.Complaint;
import com.lotosia.supportservice.entity.Suggestion;
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

    public void sendComplaintAcknowledgement(String recipientEmail) {
        String subject = "Lotosia - Şikayətiniz qəbul edildi";
        String text = "Salam,\n\nŞikayətiniz qeydə alındı. Şikayətinizə ən qısa zamanda baxılacaqdır.\n\nHörmətlə,\nLotosia Komandası";

        self.sendSimpleEmail(recipientEmail, subject, text);
    }

    public void sendSuggestionAcknowledgement(String recipientEmail) {
        String subject = "Lotosia - Təklifiniz qəbul edildi";
        String text = "Salam,\n\nTəklifinizi qəbul etdik. Bizimlə bölüşdüyünüz üçün təşəkkür edirik.\n\nLotosia komandası";

        self.sendSimpleEmail(recipientEmail, subject, text);
    }

    public void sendNewComplaintNotificationToTeam(Complaint complaint) {
        String subject = String.format("Yeni Şikayət Alındı - ID: %d", complaint.getId());
        String formattedTimestamp = complaint.getCreatedAt() != null ?
                complaint.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A";

        String text = String.format(
                """
                        Yeni bir şikayət daxil oldu:
                        
                        Şikayət ID: %d
                        Göndərən Email: %s
                        Göndərilmə Tarixi: %s
                        
                        Mesaj:
                        --------------------
                        %s
                        --------------------
                        """,
                complaint.getId(),
                complaint.getEmail(),
                formattedTimestamp,
                complaint.getMessage()
        );

        self.sendSimpleEmail(teamNotificationEmail, subject, text);
    }

    public void sendNewSuggestionNotificationToTeam(Suggestion suggestion) {
        String subject = String.format("Yeni Təklif Alındı - ID: %d", suggestion.getId());
        String formattedTimestamp = suggestion.getCreatedAt() != null ?
                suggestion.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A";

        String text = String.format(
                """
                        Yeni bir təklif daxil oldu:
                        
                        Təklif ID: %d
                        Göndərən Email: %s
                        Göndərilmə Tarixi: %s
                        
                        Mesaj:
                        --------------------
                        %s
                        --------------------
                        """,
                suggestion.getId(),
                suggestion.getEmail(),
                formattedTimestamp,
                suggestion.getMessage()
        );

        self.sendSimpleEmail(teamNotificationEmail, subject, text);
    }
}
