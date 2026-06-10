package com.rms.admin.handler.registration;

import com.rms.admin.data.dao.interfaces.IUserDao;
import com.rms.admin.data.dto.users.RegisterRequest;
import com.rms.admin.data.dto.users.UserResponse;
import com.rms.admin.exception.BadRequestException;
import com.rms.admin.exception.ConflictException;
import com.rms.admin.exception.ServiceUnavailableException;
import com.rms.admin.service.notification.VerificationEmailNotificationHandler;
import com.rms.admin.utils.constants.DefaultRole;
import com.rms.admin.utils.constants.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationHandler {
    private static final int OTP_LENGTH = 6;
    private static final String OTP_CHARS = "0123456789";

    private final IUserDao userDao;
    private final RegistrationRedisService registrationRedisService;
    private final VerificationEmailNotificationHandler verificationEmailNotificationHandler;
    private final PasswordEncoder passwordEncoder;

    public String requestVerification(String email) {
        if (userDao.existsByEmail(email)) {
            throw new ConflictException(Messages.ERROR_EMAIL_ALREADY_REGISTERED, "Email already registered");
        }
        String otp = generateOtp();
        boolean set = registrationRedisService.setOtpIfAbsent(email, otp);
        if (!set) {
            return "Verification code already sent";
        }
        try {
            verificationEmailNotificationHandler.sendVerificationEmail(email, otp);
            log.info("OTP WAS : {}", otp);
        } catch (Exception e) {
            registrationRedisService.deleteOtp(email);
            log.error("Failed to send verification email to {}", email, e);
            throw new ServiceUnavailableException("SEND_EMAIL_FAILED", "Failed to send verification email");
        }
        return "Verification code sent";
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(OTP_CHARS.charAt(random.nextInt(OTP_CHARS.length())));
        }
        return sb.toString();
    }

    public void verifyEmail(String email, String verificationCode) {
        if (!registrationRedisService.hasOtpRequest(email)) {
            throw new BadRequestException(Messages.ERROR_VERIFICATION_REQUEST_NOT_FOUND, "Request verification code first");
        }
        boolean verified = registrationRedisService.verifyOtpAndSetVerified(email, verificationCode);
        if (!verified) {
            throw new BadRequestException(Messages.ERROR_INVALID_VERIFICATION_CODE, "Invalid verification code");
        }
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (!Set.of(DefaultRole.ADMIN.getRoleId(),DefaultRole.TENANT.getRoleId()).contains(request.getRoleId())) {
            throw new BadRequestException(Messages.ERROR_INVALID_ROLE, "Role must be ADMIN or TENANT");
        }
        String verified = registrationRedisService.consumeVerifiedEmail(request.getEmail());
        if (verified == null) {
            throw new BadRequestException(Messages.ERROR_EMAIL_NOT_VERIFIED, "Complete email verification first");
        }
        if (userDao.existsByEmail(request.getEmail())) {
            throw new ConflictException(Messages.ERROR_EMAIL_ALREADY_REGISTERED, "Email already registered");
        }
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        String jwtSecret = generateJwtSecret();
        userDao.insert(request.getEmail(), hashedPassword, jwtSecret, request.getName(), request.getRoleId());
    }

    private String generateJwtSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
