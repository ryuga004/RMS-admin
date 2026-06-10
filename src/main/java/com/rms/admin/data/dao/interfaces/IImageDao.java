package com.rms.admin.data.dao.interfaces;

public interface IImageDao {
    Long insert(String objectKey);
    void deleteById(Long id);
}