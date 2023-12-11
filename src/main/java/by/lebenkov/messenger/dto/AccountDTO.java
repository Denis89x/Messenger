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

    @Size(min = 10, max = 30, message = "Адрес с почты должен корректным")
    @NotEmpty(message = "Адрес с почты должен корректным")
    @Email()
    private String email;

    @Size(min = 3, max = 20, message = "Имя должно быть длиной от 3 до 20 символов")
    @NotEmpty(message = "Имя не должно быть пустым")
    private String firstName;

    @Size(min = 3, max = 20, message = "Фамилия должна быть длиной от 3 до 20 символов")
    @NotEmpty(message = "Фамилия не должна быть пустой")
    private String lastName;


    private String password;
}