package by.lebenkov.messenger.service;

import by.lebenkov.messenger.util.UploadPictureException;
import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.contentSources.B2ContentSource;
import com.backblaze.b2.client.contentSources.B2ContentTypes;
import com.backblaze.b2.client.contentSources.B2FileContentSource;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.structures.B2FileVersion;
import com.backblaze.b2.client.structures.B2UploadFileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class LinkServiceImp implements LinkService {

    private final B2StorageService b2StorageService;

    private static final Logger logger = LoggerFactory.getLogger(LinkServiceImp.class);

    @Value("${bucket.id}")
    private String bucketId;

    @Autowired
    public LinkServiceImp(B2StorageService b2StorageService) {
        this.b2StorageService = b2StorageService;
    }

    @Override
    public String uploadProfilePicture(MultipartFile file) {
        B2StorageClient client = b2StorageService.getClient();

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        try {
            String fileName = extension + UUID.randomUUID().toString();

            File tempFile = createTempFile(file);
            File compressedFile = compressFile(tempFile);

            System.out.println("compressedFile2: " + compressedFile);

            B2ContentSource source = B2FileContentSource.build(compressedFile);

            B2UploadFileRequest request = B2UploadFileRequest.
                    builder(bucketId, fileName, B2ContentTypes.B2_AUTO, source)
                    .setCustomField("color", "green")
                    .build();

            B2FileVersion fileVersion = client.uploadSmallFile(request);

            System.out.println("tempFile: " + tempFile);

            tempFile.delete();
            //compressedFile.delete();

            return "https://f005.backblazeb2.com/file/lebenkovMessenger/" + fileVersion.getFileName();
        } catch (B2Exception e) {
            logger.error("Ошибка при загрузке фотографии", e);
            throw new UploadPictureException("Ошибка при загрузке фотографии!", e);
        }
    }

    private File compressFile(File tempFile) {
        try {
            File compressedFile = File.createTempFile("compressed-", ".tmp");
            System.out.println("Compressing file: " + tempFile.getAbsolutePath());

            Thumbnails.of(tempFile)
                    .size(200, 200)
                    .outputFormat("png")
                    .toOutputStream(new FileOutputStream(compressedFile));

            System.out.println("Compressed file created: " + compressedFile.getAbsolutePath());
            System.out.println("Compressed file created: " + compressedFile);
            return compressedFile;
        } catch (IOException e) {
            logger.error("Ошибка при сжатии фотографии", e);
        }
        return null;
    }

    private File createTempFile(MultipartFile file) {
        try {
            Path tempFilePath = Files.createTempFile("temp-", null);
            Files.write(tempFilePath, file.getBytes());
            return tempFilePath.toFile();
        } catch (IOException e) {
            logger.error("Ошибка при создании временного файла", e);
            return null;
        }
    }
}
