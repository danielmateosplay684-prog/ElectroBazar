package com.proconsi.electrobazar.service.impl;

import com.proconsi.electrobazar.model.AppSetting;
import com.proconsi.electrobazar.repository.AppSettingRepository;
import com.proconsi.electrobazar.util.AesEncryptionUtil;
import com.proconsi.electrobazar.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final AppSettingRepository appSettingRepository;
    private final AesEncryptionUtil aesEncryptionUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Override
    public void sendEmail(String to, String subject, String body) {
        sendResendEmail(to, subject, body, null);
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, File attachment) {
        try {
            byte[] content = Files.readAllBytes(attachment.toPath());
            sendEmailWithAttachment(to, subject, body, attachment.getName(), content);
        } catch (IOException e) {
            log.error("Failed to read attachment file", e);
            throw new RuntimeException("Email sending failed: cannot read attachment", e);
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentName, byte[] attachmentContent) {
        Map<String, String> attachment = new HashMap<>();
        attachment.put("filename", attachmentName);
        attachment.put("content", Base64.getEncoder().encodeToString(attachmentContent));
        sendResendEmail(to, subject, body, Collections.singletonList(attachment));
    }

    private void sendResendEmail(String to, String subject, String htmlBody, List<Map<String, String>> attachments) {
        String apiKeyEncrypted = appSettingRepository.findByKey("mail.api_key").map(AppSetting::getValue).orElse("");
        String apiKey = aesEncryptionUtil.decrypt(apiKeyEncrypted);
        String from = appSettingRepository.findByKey("mail.from").map(AppSetting::getValue).orElse("onboarding@resend.dev");

        if (apiKey.isBlank()) {
            log.warn("Resend API key is missing. Email will not be sent.");
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("from", from);
            payload.put("to", Collections.singletonList(to));
            payload.put("subject", subject);
            payload.put("html", htmlBody);
            if (attachments != null && !attachments.isEmpty()) {
                payload.put("attachments", attachments);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(RESEND_API_URL, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent successfully via Resend to {}", to);
            } else {
                log.error("Failed to send email via Resend. Status: {}, Response: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Resend API error: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("Exception while sending email via Resend", e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
