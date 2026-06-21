package com.bookheaven.user_service.service.impl;

import com.bookheaven.user_service.constant.AppConstants;
import com.bookheaven.user_service.dto.requestDto.SendOtpRequest;
import com.bookheaven.user_service.exception.InvalidOtpException;
import com.bookheaven.user_service.exception.OtpRateLimitException;
import com.bookheaven.user_service.service.OtpService;
import com.bookheaven.user_service.service.RedisService;
import com.bookheaven.user_service.service.clientService.EmailClient;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@AllArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private RedisService redisService;
    private PasswordEncoder passwordEncoder;
    private EmailClient emailClient;

    public String generateOtp(String email){
        String cooldownKey = AppConstants.REDIS_KEY_OTP_COOLDOWN + email;
        String countKey = AppConstants.REDIS_KEY_OTP_COUNT + email;
        if (redisService.containsKey(cooldownKey)) {
            throw new OtpRateLimitException("Please wait before requesting OTP again");
        }
        Integer count = null;
        if(redisService.containsKey(countKey)){
            count = Integer.parseInt(redisService.getKey(countKey));
            if(count >= 5)  throw new OtpRateLimitException("Too many OTP requests. Try again later");
        }
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        redisService.saveKeyWithTimeout(AppConstants.REDIS_KEY_OTP + email, passwordEncoder.encode(otp), AppConstants.OTP_EXPIRY_MINUTES);
        redisService.saveKeyWithTimeout(cooldownKey,"1",AppConstants.OTP_COOLDOWN_MINUTES);
        if (null == count) {
            redisService.saveKeyWithTimeout(countKey, "1", AppConstants.OTP_MAX_ATTEMPTS_MINUTES);
        } else {
            redisService.incrementValueByOne(countKey);
        }
        return otp;
    }

    public void sendOtp(String email) throws MessagingException {
        String otp = generateOtp(email);
        log.debug("Otp generated");
        SendOtpRequest request = new SendOtpRequest();
        request.setOtp(otp);
        request.setTo(email);
        emailClient.sendOtp(request);
    }

    public void validateOtp(String email, String otp){
        String otpKey = AppConstants.REDIS_KEY_OTP + email;
        if(!redisService.containsKey(otpKey)){
            throw new InvalidOtpException("OTP expired, please try again");
        }
        String storedOtp = redisService.getKey(otpKey);
        if(passwordEncoder.matches(otp, storedOtp)){
            redisService.deleteKey(otpKey);
        }else{
            throw new InvalidOtpException("Invalid OTP");
        }
    }
}
