package by.lebenkov.messenger.util;

import by.lebenkov.messenger.dto.ChangePasswordDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ChangePasswordValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return ChangePasswordDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentPassword", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassword", "field.required");

        ChangePasswordDTO changePasswordDTO = (ChangePasswordDTO) target;

        if (!isValidPassword(changePasswordDTO.getNewPassword())) {
            errors.rejectValue("newPassword", "password.invalidFormat");
        }
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 4 || password.length() > 15) {
            return false;
        }

        if (!password.matches(".*[a-zA-Z].*")) {
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            return false;
        }

        return password.matches(".*[!@#$%^&*()].*");
    }
}
