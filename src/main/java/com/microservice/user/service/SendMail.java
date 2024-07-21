package com.microservice.user.service;
import com.microservice.user.dto.MailRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@Service
public class SendMail {

    private static final Logger logger = LoggerFactory.getLogger(SendMail.class);

    private final RestTemplate restTemplate;

    @Value("${mail.service.url}")
    private String mailServiceUrl;

    public SendMail(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String sendMail(List<String> toUser, String subject, String message) {
        MailRequestDTO mailRequestDTO = new MailRequestDTO(toUser, subject, message);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MailRequestDTO> request = new HttpEntity<>(mailRequestDTO, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(mailServiceUrl, request, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            logger.error("Error sending mail: {}", e.getMessage());
            return "Error sending mail: " + e.getMessage();
        }
    }
}
