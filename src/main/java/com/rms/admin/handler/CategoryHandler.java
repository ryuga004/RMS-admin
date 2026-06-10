package com.rms.admin.handler;

import com.rms.admin.data.dao.interfaces.ICategoryDao;
import com.rms.admin.data.dto.category.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryHandler {

    private final ICategoryDao categoryDao;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryDao.findAll();
    }
}
