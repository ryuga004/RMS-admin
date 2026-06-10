package com.rms.admin.handler.registration;

import com.rms.admin.utils.constants.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationRedisService {

    private final StringRedisTemplate redisTemplate;

    @Value("${registration.otp.ttl-minutes:5}")
    private int otpTtlMinutes;

    @Value("${registration.verified.ttl-minutes:3}")
    private int verifiedTtlMinutes;

    public boolean setOtpIfAbsent(String email, String otp) {
        String key = Messages.REDIS_KEY_PREFIX_OTP + email;
        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, otp, Duration.ofMinutes(otpTtlMinutes));
        return Boolean.TRUE.equals(set);
    }

    public void deleteOtp(String email) {
        String key = Messages.REDIS_KEY_PREFIX_OTP + email;
        redisTemplate.delete(key);
    }

    public boolean hasOtpRequest(String email) {
        String key = Messages.REDIS_KEY_PREFIX_OTP + email;
        return redisTemplate.hasKey(key);
    }

    public boolean verifyOtpAndSetVerified(String email, String otp) {
        String otpKey = Messages.REDIS_KEY_PREFIX_OTP + email;
        String verifiedKey = Messages.REDIS_KEY_PREFIX_VERIFIED + email;
        String currentOtp = redisTemplate.opsForValue().get(otpKey);
        if (currentOtp == null || !currentOtp.equals(otp)) {
            return false;
        }
        redisTemplate.delete(otpKey);
        redisTemplate.opsForValue().set(verifiedKey, Messages.REDIS_VERIFIED_VALUE, Duration.ofMinutes(verifiedTtlMinutes));
        return true;
    }

    public String consumeVerifiedEmail(String email) {
        String key = Messages.REDIS_KEY_PREFIX_VERIFIED + email;
        return redisTemplate.opsForValue().getAndDelete(key);
    }
}