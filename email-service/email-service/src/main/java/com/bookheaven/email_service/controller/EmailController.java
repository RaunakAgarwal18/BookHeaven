package com.bookheaven.email_service.controller;

import com.bookheaven.email_service.dto.response.EmailResponse;
import com.bookheaven.email_service.dto.request.PasswordResetRequest;
import com.bookheaven.email_service.dto.request.SendOtpRequest;
import com.bookheaven.email_service.service.EmailService;
import com.bookheaven.email_service.util.ApiUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send-otp")
    public ResponseEntity<EmailResponse> sendOtp(@RequestBody SendOtpRequest request) {
        emailService.processEmailEvent(request);
        return ResponseEntity.ok(ApiUtil.success("OTP email sent successfully"));
    }

    @PostMapping("/send-password-reset")
    public ResponseEntity<EmailResponse> sendPasswordReset(@RequestBody PasswordResetRequest request){
        emailService.processEmailEvent(request);
        return ResponseEntity.ok(ApiUtil.success("Password Reset email sent successfully"));
    }
}