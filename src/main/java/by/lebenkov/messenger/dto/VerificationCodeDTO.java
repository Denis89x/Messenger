package by.lebenkov.messenger.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationCodeDTO {

    private Integer userId;
    private String verificationCode;
}
