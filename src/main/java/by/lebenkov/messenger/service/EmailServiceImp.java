package by.lebenkov.messenger.service;

import by.lebenkov.messenger.util.EmailSendingException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Random;

@Service
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class EmailServiceImp implements EmailService {

    JavaMailSender mailSender;
    static Logger logger = LoggerFactory.getLogger(EmailServiceImp.class);

    @Override
    public void sendVerificationCode(String email, String verificationCode) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;

        try {
            helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setFrom("pastebinp@mail.ru");
            helper.setSubject("Verification Code for Your Account");
            helper.setText("Your verification code is: " + verificationCode);

            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Ошибка при отправке письма на адрес: {}", email, e);
            throw new EmailSendingException("Ошибка при отправке письма!", e);
        }
    }

    @Override
    public String getEmailMasked(String email) {
        int atIndex = email.indexOf("@");
        if (atIndex > 1) {
            String username = email.substring(0, atIndex);
            int length = username.length();
            String masked = username.charAt(0) + "*".repeat(length - 2) + username.charAt(length - 1);
            return masked + email.substring(atIndex);
        }
        return email;
    }

    @Override
    public String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
