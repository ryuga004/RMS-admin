package com.rms.admin.controller;

import com.rms.admin.data.dto.ApiResponse;
import com.rms.admin.data.dto.PaginationResponse;
import com.rms.admin.data.dto.users.UserProfileResponse;
import com.rms.admin.data.dto.users.UserResponse;
import com.rms.admin.data.dto.users.profile.ProfileUpdateRequest;
import com.rms.admin.handler.UserHandler;
import com.rms.admin.security.JwtPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserHandler userHandler;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TENANT')")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal JwtPrincipal principal) {
        log.debug("GET /users/me for userId={}", principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(userHandler.getUserById(principal.getUserId())));
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TENANT')")
    public ResponseEntity<ApiResponse<PaginationResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "20") @Min(1) int pageSize,
            @RequestParam(required = false) String searchText) {
        PaginationResponse response = userHandler.getAllUsers(pageNumber, pageSize, searchText);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable @Valid @NotNull Long id) {
        return ResponseEntity.ok(ApiResponse.success(userHandler.getUserById(id)));
    }

    @GetMapping("/{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TENANT')")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@PathVariable @Valid @NotNull Long id) {
        return ResponseEntity.ok(ApiResponse.success(userHandler.getProfile(id)));
    }

    @PutMapping("/{id}/profile")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TENANT')")
    public ResponseEntity<ApiResponse<String>> updateProfile(@PathVariable @Valid @NotNull Long id, @RequestBody @Valid ProfileUpdateRequest request) {
        userHandler.updateProfile(id, request);
        return ResponseEntity.ok(ApiResponse.success("Updated Successfully"));
    }
    

    @PutMapping("/{id}/profile/image")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TENANT') or #id.equals(principal.userId)")
    public ResponseEntity<ApiResponse<String>> updateProfileImage(
            @PathVariable @Valid @NotNull Long id,
            @RequestParam("file") MultipartFile file) {
        userHandler.updateProfileImage(id, file);
        return ResponseEntity.ok(ApiResponse.success("Updated Successfully"));
    }
}
