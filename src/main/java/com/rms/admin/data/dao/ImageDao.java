package com.rms.admin.data.dao;

import com.rms.admin.data.dao.interfaces.IImageDao;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.rms.admin.persistence.tables.Images.IMAGES;

@Repository
@RequiredArgsConstructor
public class ImageDao implements IImageDao {

    private final DSLContext dsl;

    @Override
    public Long insert(String objectKey) {
        return dsl.insertInto(IMAGES)
                .columns(IMAGES.KEY)
                .values(objectKey)
                .returningResult(IMAGES.ID)
                .fetchOneInto(Long.class);
    }

    @Override
    public void deleteById(Long id) {
        dsl.deleteFrom(IMAGES).where(IMAGES.ID.eq(id)).execute();
    }
}
