package by.lebenkov.messenger.util;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

@Component
public class UploadPictureValidator implements Validator {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    @Override
    public boolean supports(Class<?> clazz) {
        return MultipartFile.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MultipartFile file = (MultipartFile) target;

        if (file.isEmpty()) {
            errors.rejectValue("file", "", "Please select a file to upload.");
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            errors.rejectValue("file", "", "File size exceeds the maximum allowed limit (5MB).");
        }
    }
}
