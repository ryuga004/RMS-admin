package com.rms.admin.utils.constants;

public interface Messages {
    public static final String REDIS_KEY_PREFIX_OTP = "registration:otp:";
    public static final String REDIS_KEY_PREFIX_VERIFIED = "registration:verified:";
    public static final String REDIS_VERIFIED_VALUE = "1";
    public static final String ERROR_EMAIL_ALREADY_REGISTERED = "EMAIL_ALREADY_REGISTERED";
    public static final String ERROR_VERIFICATION_REQUEST_NOT_FOUND = "VERIFICATION_REQUEST_NOT_FOUND";
    public static final String ERROR_INVALID_VERIFICATION_CODE = "INVALID_VERIFICATION_CODE";
    public static final String ERROR_EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";
    public static final String ERROR_INVALID_ROLE = "INVALID_ROLE";
    public static final String ERROR_TEMPLATE_NOT_FOUND = "EMAIL_TEMPLATE_NOT_FOUND";
}