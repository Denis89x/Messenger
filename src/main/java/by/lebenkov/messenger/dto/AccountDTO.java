package by.lebenkov.messenger.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
public class AccountDTO {

    @Size(min = 3, max = 12, message = "Логин должен быть длины от 3 до 12 символов")
    @NotEmpty(message = "Логин не должен быть пустым")
    private String username;

    @Size(min = 10, max = 30, message = "Адресс почты должен корректным")
    @NotEmpty(message = "Адресс почты должен корректным")
    @Email()
    private String email;

    private String password;
}