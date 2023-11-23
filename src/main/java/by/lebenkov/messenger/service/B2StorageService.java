package by.lebenkov.messenger.service;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.B2StorageClientFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@NoArgsConstructor
public class B2StorageService {

    @Getter
    private B2StorageClient client;

    @Value("${application.key.id}")
    private String applicationKeyId;

    @Value("${application.key}")
    private String applicationKey;

    @PostConstruct
    public void init() {
        try {
            client = B2StorageClientFactory
                    .createDefaultFactory()
                    .create(applicationKeyId, applicationKey, "Messenger/1.0");
        } catch (Exception e) {
            log.info("Failed to initialize B2StorageService: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize B2StorageService", e);
        }
    }
}
