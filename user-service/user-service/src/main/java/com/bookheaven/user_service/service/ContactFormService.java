package com.bookheaven.user_service.service;

import com.bookheaven.user_service.dto.requestDto.ContactRequest;

public interface ContactFormService {
    void submitContactForm(ContactRequest request);
}
