package com.rms.admin.data.dao;

import com.rms.admin.data.dao.interfaces.ICategoryDao;
import com.rms.admin.data.dto.category.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.rms.admin.persistence.tables.Category.CATEGORY;

@Repository
@RequiredArgsConstructor
public class CategoryDao implements ICategoryDao {

    private final DSLContext dsl;

    @Override
    public boolean existsById(Long id) {
        return dsl.fetchExists(CATEGORY, CATEGORY.ID.eq(id));
    }

    @Override
    public List<CategoryResponse> findAll() {
        return dsl.select(CATEGORY.ID, CATEGORY.NAME)
                .from(CATEGORY)
                .orderBy(CATEGORY.NAME)
                .fetchInto(CategoryResponse.class);
    }
}
