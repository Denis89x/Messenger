package by.lebenkov.messenger.service;

public interface EmailService {
    void sendVerificationCode(String email, String verificationCode);

    String getEmailMasked(String email);

    String generateVerificationCode();
}
