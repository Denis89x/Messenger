package by.lebenkov.messenger.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Service
public class CaptchaService implements ICaptchaService {

    @Value("${google.recaptcha.key.secret}")
    private String secretKey;

    @Override
    public boolean isCaptchaValid(String recaptchaResponse) {
        String params = "secret=" + secretKey + "&response=" + recaptchaResponse;

        try {
            BufferedReader bufferedReader = getBufferedReader(params);
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();

            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
            return jsonObject.get("success").getAsBoolean();

        } catch (IOException e) {
            log.info("Invalid Captcha");
            return false;
        }
    }

    private BufferedReader getBufferedReader(String params) throws IOException {
        String url = "https://www.google.com/recaptcha/api/siteverify";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
        outputStreamWriter.write(params);
        outputStreamWriter.flush();
        outputStreamWriter.close();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        return bufferedReader;
    }
}
