package by.lebenkov.messenger.service;

public interface ICaptchaService {
    boolean isCaptchaValid(String recaptchaResponse);
}
