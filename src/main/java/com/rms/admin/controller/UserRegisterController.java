package com.rms.admin.controller;

import com.rms.admin.data.dto.ApiResponse;
import com.rms.admin.data.dto.users.RegisterRequest;
import com.rms.admin.data.dto.users.VerificationRequestRequest;
import com.rms.admin.data.dto.users.VerifyEmailRequest;
import com.rms.admin.handler.registration.UserRegistrationHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class UserRegisterController {

    private final UserRegistrationHandler userRegistrationHandler;

    @PostMapping("/verification-request")
    public ResponseEntity<ApiResponse<String>> requestVerification(@RequestBody @Valid VerificationRequestRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userRegistrationHandler.requestVerification(request.getEmail())));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        userRegistrationHandler.verifyEmail(request.getEmail(), request.getVerificationCode());
        return ResponseEntity.ok(ApiResponse.success("Email verified"));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid RegisterRequest request) {
        userRegistrationHandler.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
