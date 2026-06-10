package com.rms.admin.data.dao.interfaces;

import com.rms.admin.data.dto.category.CategoryResponse;

import java.util.List;

public interface ICategoryDao {
    boolean existsById(Long id);
    List<CategoryResponse> findAll();
}
