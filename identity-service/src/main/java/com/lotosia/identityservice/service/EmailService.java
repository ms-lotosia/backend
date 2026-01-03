package com.lotosia.identityservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmailAddress;

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

    @Async
    public void sendOtpEmailHtml(String recipientEmail, String otp) {

        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setTo(recipientEmail);
            helper.setFrom(fromEmailAddress);
            helper.setSubject("Lotosia - Qeydiyyat üçün OTP Kodu");

            String plainText = "Lotosia qeydiyyatı üçün OTP kodunuz\n\n"
                    + "OTP kodunuz: " + otp + "\n"
                    + "Bu kod 5 dəqiqə ərzində keçərlidir.\n\n"
                    + "Bu kodu heç kəslə paylaşmayın.";

            String html = "<div style=\"font-family:Inter,Arial,sans-serif;background:#f3f4f6;padding:24px;\">"
                    + "  <span style=\"display:none!important;visibility:hidden;opacity:0;height:0;width:0;overflow:hidden;\">Lotosia qeydiyyatı üçün OTP kodunuz. Kod 5 dəqiqə keçərlidir.</span>"
                    + "  <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width:600px;margin:0 auto;\">"
                    + "    <tr><td>"
                    + "      <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#eaeef3;border-radius:8px;\">"
                    + "        <tr><td style=\"padding:32px\">"
                    + "          <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:6px;text-align:center;\">"
                    + "            <tr><td style=\"padding:28px 24px 8px 24px\">"
                    + "              <div style=\"font-weight:700;font-size:18px;color:#0f172a;\">Lotosia</div>"
                    + "            </td></tr>"
                    + "            <tr><td style=\"padding:4px 24px 12px 24px\">"
                    + "              <h1 style=\"margin:0;color:#0f172a;font-size:20px;line-height:1.4;\">Qeydiyyat Təsdiqi</h1>"
                    + "              <p style=\"margin:12px 0 0 0;color:#475569;\">Hesabınızı yaratmaq üçün aşağıdakı OTP kodunu daxil edin.</p>"
                    + "            </td></tr>"
                    + "            <tr><td style=\"padding:20px 24px 8px 24px\">"
                    + "              <div style=\"background:#f1f5f9;border:2px dashed #cbd5e1;border-radius:8px;padding:24px;margin:0 auto;max-width:200px;\">"
                    + "                <div style=\"font-size:32px;font-weight:700;color:#0f172a;letter-spacing:4px;\">" + otp + "</div>"
                    + "              </div>"
                    + "            </td></tr>"
                    + "            <tr><td style=\"padding:8px 24px 24px 24px\">"
                    + "              <p style=\"margin:0;color:#475569;font-size:14px;\">Bu kod 5 dəqiqə ərzində keçərlidir.</p>"
                    + "              <p style=\"margin:8px 0 0 0;color:#dc2626;font-size:12px;font-weight:600;\">⚠️ Bu kodu heç kəslə paylaşmayın</p>"
                    + "            </td></tr>"
                    + "          </table>"
                    + "        </td></tr>"
                    + "      </table>"
                    + "      <p style=\"text-align:center;color:#94a3b8;font-size:12px;margin:12px 0 0 0\">© " + java.time.Year.now() + " Lotosia</p>"
                    + "    </td></tr>"
                    + "  </table>"
                    + "</div>";

            helper.setText(plainText, html);
            mailSender.send(mimeMessage);

        } catch (Exception e) {
            String subject = "Lotosia - Qeydiyyat üçün OTP Kodu";
            String text = "OTP kodunuz: " + otp + "\n" +
                    "Bu kod 5 dəqiqə ərzində keçərlidir.\n\n" +
                    "Bu kodu heç kəslə paylaşmayın.";
            try {
                self.sendSimpleEmail(recipientEmail, subject, text);
            } catch (Exception ignored) {
            }
        }
    }

    @Async
    public void sendResetPasswordEmailHtml(String recipientEmail, String resetLink) {

        try {
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setTo(recipientEmail);
            helper.setFrom(fromEmailAddress);
            helper.setSubject("Reset Your Lotosia Password");

            String accountEmail = recipientEmail;

            String plainText = "Reset your Lotosia password\n\n"
                    + "We received a request to reset the password for: " + accountEmail + "\n"
                    + "This link expires in 1 hour.\n\n"
                    + "Reset password: " + resetLink + "\n\n"
                    + "If you didn't request this, you can ignore this email or contact support at " + fromEmailAddress + ".";

            String html = "<div style=\"font-family:Inter,Arial,sans-serif;background:#f3f4f6;padding:24px;\">"
                    + "  <span style=\"display:none!important;visibility:hidden;opacity:0;height:0;width:0;overflow:hidden;\">Reset your Lotosia password. Link expires in 1 hour.</span>"
                    + "  <table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"max-width:600px;margin:0 auto;\">"
                    + "    <tr><td>"
                    + "      <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#eaeef3;border-radius:8px;\">"
                    + "        <tr><td style=\"padding:32px\">"
                    + "          <table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#ffffff;border-radius:6px;text-align:center;\">"
                    + "            <tr><td style=\"padding:28px 24px 8px 24px\">"
                    + "              <div style=\"font-weight:700;font-size:18px;color:#0f172a;\">Lotosia</div>"
                    + "            </td></tr>"
                    + "            <tr><td style=\"padding:4px 24px 12px 24px\">"
                    + "              <h1 style=\"margin:0;color:#0f172a;font-size:20px;line-height:1.4;\">Password Reset</h1>"
                    + "              <p style=\"margin:12px 0 0 0;color:#475569;\">If you've lost your password or wish to reset it, use the link below to get started.</p>"
                    + "            </td></tr>"
                    + "            <tr><td style=\"padding:4px 24px 0 24px\">"
                    + "              <p style=\"margin:0;color:#0f172a;font-weight:600;\">" + accountEmail + "</p>"
                    + "              <p style=\"margin:8px 0 0 0;color:#475569;\">For security, this link expires in 1 hour.</p>"
                    + "            </td></tr>"
                    + "            <tr><td style=\"padding:20px 24px 8px 24px\">"
                    + "              <a href=\"" + resetLink + "\" target=\"_blank\" rel=\"noopener\" style=\"display:inline-block;background:#3b82f6;color:#ffffff;text-decoration:none;padding:12px 20px;border-radius:6px;font-weight:600;\">Reset Your Password</a>"
                    + "            </td></tr>"
                    + "            <tr><td style=\"padding:8px 24px 24px 24px\">"
                    + "              <p style=\"margin:0;color:#94a3b8;font-size:12px;\">If you did not request a password reset, you can safely ignore this email. Only a person with access to your email can reset your account password.</p>"
                    + "              <p style=\"margin:10px 0 0 0;color:#64748b;font-size:12px;\">Having trouble? Copy and paste this URL into your browser:</p>"
                    + "              <p style=\"margin:6px 0 0 0;word-break:break-all;font-size:12px;\"><a href=\"" + resetLink + "\" style=\"color:#2563eb;\">" + resetLink + "</a></p>"
                    + "            </td></tr>"
                    + "          </table>"
                    + "        </td></tr>"
                    + "      </table>"
                    + "      <p style=\"text-align:center;color:#94a3b8;font-size:12px;margin:12px 0 0 0\">© " + java.time.Year.now() + " Lotosia</p>"
                    + "    </td></tr>"
                    + "  </table>"
                    + "</div>";

            helper.setText(plainText, html);
            mailSender.send(mimeMessage);

        } catch (Exception e) {
            String subject = "Reset Your Lotosia Password";
            String text = "Click the link to reset your password: " + resetLink + "\n\n"
                    + "If you didn't request this, you can ignore this email.";
            try {
                self.sendSimpleEmail(recipientEmail, subject, text);
            } catch (Exception ignored) {
            }
        }
    }
}
