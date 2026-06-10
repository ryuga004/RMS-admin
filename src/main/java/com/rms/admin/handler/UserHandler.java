package com.rms.admin.handler;

import com.rms.admin.config.StorageProperties;
import com.rms.admin.data.dao.AddressDao;
import com.rms.admin.data.dao.interfaces.IAddressDao;
import com.rms.admin.data.dao.interfaces.IImageDao;
import com.rms.admin.data.dao.interfaces.IUserDao;
import com.rms.admin.data.dto.PaginationResponse;
import com.rms.admin.data.dto.users.*;
import com.rms.admin.data.dto.users.profile.ProfileUpdateRequest;
import com.rms.admin.exception.BadRequestException;
import com.rms.admin.service.storage.S3StorageService;
import com.rms.admin.service.storage.StorageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.rms.admin.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserHandler {

    private static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    private static final String INVALID_USER_ID = "INVALID_USER_ID";
    private static final String PROFILE_IMAGE_EMPTY = "PROFILE_IMAGE_EMPTY";

    private final IUserDao userDao;
    private final IImageDao imageDao;
    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final IAddressDao addressDao;

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        UserResponse user = userDao.findById(userId);
        if (user == null) {
            throw new NotFoundException(USER_NOT_FOUND);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        UserProfileResult result = userDao.findProfileByUserId(userId);
        if (result == null) {
            throw new NotFoundException(USER_NOT_FOUND);
        }
        String profileImageUrl = null;
        if (result.getImageKey() != null) {
            profileImageUrl = storageService.generatePresignedUrl(
                    result.getImageKey(),
                    storageProperties.getPresignedExpiryMinutes());
        }
        Address address = null;
        if (result.getLocalAddress() != null || result.getCity() != null
                || result.getState() != null || result.getCountry() != null) {
            address = Address.builder()
                    .localAddress(result.getLocalAddress())
                    .city(result.getCity())
                    .state(result.getState())
                    .country(result.getCountry())
                    .build();
        }
        return UserProfileResponse.builder()
                .id(result.getId())
                .name(result.getName())
                .email(result.getEmail())
                .roleId(result.getRoleId())
                .roleName(result.getRoleName())
                .phoneNo(result.getPhoneNo())
                .isVerified(result.getIsVerified())
                .createdAt(result.getCreatedAt())
                .profileImageUrl(profileImageUrl)
                .address(address)
                .build();
    }

    @Transactional(readOnly = true)
    public PaginationResponse getAllUsers(int page, int limit, String searchText) {
        List<UserResponse> results = userDao.findAll(page, limit, searchText);
        long totalCount = userDao.countAll(searchText);
        return PaginationResponse.builder()
                .result(results)
                .totalCount(totalCount)
                .build();
    }

    @Transactional
    public void updateProfileImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException(PROFILE_IMAGE_EMPTY, "Profile image file is required");
        }
        ImageIdKey oldImage = userDao.getImageIdAndKeyByUserId(userId);
        String newKey = S3StorageService.buildUserProfileImageKey(userId);
        storageService.uploadFile(file, newKey);
        Long newImageId = imageDao.insert(newKey);
        userDao.updateUserImageId(userId, newImageId);
        if (oldImage != null) {
            storageService.deleteFile(oldImage.getObjectKey());
            imageDao.deleteById(oldImage.getImageId());
        }
    }

    @Transactional
    public void updateProfile(@Valid @NotNull Long id, ProfileUpdateRequest request) {
        Long addressId = userDao.getAddressIdAndSetPhoneNo(id,request.getPhoneNo());
        Address address = request.getAddress();
        if(addressId==null) {
            Long createdAddressId = addressDao.insert(address.getLocalAddress(),address.getCity(),address.getState(), address.getCountry());
            userDao.updateUserAddressId(id,createdAddressId);
        } else {
            addressDao.update(addressId,address);
        }
    }
}
