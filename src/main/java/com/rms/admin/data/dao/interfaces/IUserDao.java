package com.rms.admin.data.dao.interfaces;

import com.rms.admin.data.dto.users.ImageIdKey;
import com.rms.admin.data.dto.users.UserProfileResult;
import com.rms.admin.data.dto.users.UserResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;

public interface IUserDao {

    UserResponse findById(Long id);

    UserProfileResult findProfileByUserId(Long userId);

    ImageIdKey getImageIdAndKeyByUserId(Long userId);

    void updateUserImageId(Long userId, Long imageId);

    Optional<String> findJwtSecretByUserId(Long userId);

    boolean existsByEmail(String email);

    void insert(String email, String hashedPassword, String jwtSecret, String name, Long roleId);

    Long getAddressIdAndSetPhoneNo(@Valid @NotNull Long id, String phoneNo);

    void updateUserAddressId(Long userId, Long addressId);

    List<UserResponse> findAll(int page, int limit, String searchText);

    long countAll(String searchText);
}
