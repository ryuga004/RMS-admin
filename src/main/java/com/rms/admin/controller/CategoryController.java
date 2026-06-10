package com.rms.admin.controller;

import com.rms.admin.data.dto.ApiResponse;
import com.rms.admin.data.dto.category.CategoryResponse;
import com.rms.admin.handler.CategoryHandler;
import com.rms.admin.security.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryHandler categoryHandler;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @AuthenticationPrincipal JwtPrincipal principal) {
        List<CategoryResponse> categories = categoryHandler.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
