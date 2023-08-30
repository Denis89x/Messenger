package by.lebenkov.messenger.service;

import com.backblaze.b2.client.exceptions.B2Exception;
import org.springframework.web.multipart.MultipartFile;

public interface LinkService {
    String uploadProfilePicture(MultipartFile file) throws B2Exception;
}
