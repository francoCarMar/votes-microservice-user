package com.microservice.user.dto;

import java.util.List;

public record MailRequestDTO(List<String> toUser, String subject, String message) {
}
