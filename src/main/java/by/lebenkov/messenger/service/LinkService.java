package by.lebenkov.messenger.service;

import org.springframework.web.multipart.MultipartFile;

public interface LinkService {
    String uploadProfilePicture(MultipartFile file);
}
