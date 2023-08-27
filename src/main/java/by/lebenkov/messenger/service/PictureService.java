package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Account;
import com.backblaze.b2.client.exceptions.B2Exception;
import org.springframework.web.multipart.MultipartFile;

public interface PictureService {
    String uploadProfilePicture(MultipartFile file) throws B2Exception;
}
