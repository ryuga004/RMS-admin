package com.rms.admin.data.dao;

import com.rms.admin.data.dao.interfaces.IUserDao;
import com.rms.admin.data.dto.users.ImageIdKey;
import com.rms.admin.data.dto.users.UserProfileResult;
import com.rms.admin.data.dto.users.UserResponse;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Condition;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.rms.admin.persistence.tables.Address.ADDRESS;
import static com.rms.admin.persistence.tables.Images.IMAGES;
import static com.rms.admin.persistence.tables.Roles.ROLES;
import static com.rms.admin.persistence.tables.Users.USERS;

@Repository
@RequiredArgsConstructor
public class UserDao implements IUserDao {

    private final DSLContext dsl;

    @Override
    public UserResponse findById(Long id) {
        return dsl.select(USERS.ID, USERS.NAME, USERS.EMAIL, USERS.ROLE_ID)
                .from(USERS)
                .where(USERS.ID.eq(id))
                .fetchOneInto(UserResponse.class);
    }

    @Override
    public UserProfileResult findProfileByUserId(Long userId) {
        return dsl.select(
                        USERS.ID.as("id"),
                        USERS.NAME.as("name"),
                        USERS.EMAIL.as("email"),
                        USERS.ROLE_ID.as("roleId"),
                        ROLES.NAME.as("roleName"),
                        USERS.PHONE_NO.as("phoneNo"),
                        USERS.IS_VERIFIED.as("isVerified"),
                        USERS.CREATED_AT.as("createdAt"),
                        IMAGES.KEY.as("imageKey"),  
                        ADDRESS.LOCAL_ADDRESS.as("localAddress"),
                        ADDRESS.CITY.as("city"),
                        ADDRESS.STATE.as("state"),
                        ADDRESS.COUNTRY.as("country"))
                .from(USERS)
                .leftJoin(IMAGES).on(USERS.IMAGE_ID.eq(IMAGES.ID))
                .leftJoin(ADDRESS).on(USERS.ADDRESS_ID.eq(ADDRESS.ID))
                .leftJoin(ROLES).on(USERS.ROLE_ID.eq(ROLES.ID))
                .where(USERS.ID.eq(userId))
                .fetchOneInto(UserProfileResult.class);
    }

    @Override
    public ImageIdKey getImageIdAndKeyByUserId(Long userId) {
        return dsl.select(IMAGES.ID, IMAGES.KEY)
                .from(USERS)
                .innerJoin(IMAGES).on(USERS.IMAGE_ID.eq(IMAGES.ID))
                .where(USERS.ID.eq(userId))
                .fetchOneInto(ImageIdKey.class);
    }

    @Override
    public void updateUserImageId(Long userId, Long imageId) {
        dsl.update(USERS)
                .set(USERS.IMAGE_ID, imageId)
                .where(USERS.ID.eq(userId))
                .execute();
    }

    @Override
    public Optional<String> findJwtSecretByUserId(Long userId) {
        return dsl.select(USERS.JWT_SECRET)
                .from(USERS)
                .where(USERS.ID.eq(userId))
                .fetchOptional(USERS.JWT_SECRET);
    }

    @Override
    public boolean existsByEmail(String email) {
        return dsl.fetchExists(USERS, USERS.EMAIL.eq(email));
    }

    @Override
    public void insert(String email, String hashedPassword, String jwtSecret, String name, Long roleId) {
        dsl.insertInto(USERS)
                .columns(USERS.NAME, USERS.EMAIL, USERS.PASSWORD, USERS.JWT_SECRET, USERS.ROLE_ID, USERS.IS_VERIFIED)
                .values(name, email, hashedPassword, jwtSecret, roleId, true)
                .execute();
    }

    @Override
    public Long getAddressIdAndSetPhoneNo(Long id, String phoneNo) {
        return dsl.update(USERS).set(USERS.PHONE_NO, phoneNo).where(USERS.ID.eq(id)).returningResult(USERS.ADDRESS_ID).fetchOneInto(Long.class);
    }

    @Override
    public void updateUserAddressId(Long userId, Long addressId) {
        dsl.update(USERS).set(USERS.ADDRESS_ID,addressId).where(USERS.ID.eq(userId)).execute();
    }

    @Override
    public List<UserResponse> findAll(int page, int limit, String searchText) {
        int offset = page * limit;
        Condition searchCondition = buildSearchCondition(searchText);
        return dsl.select(USERS.ID, USERS.NAME, USERS.EMAIL, USERS.ROLE_ID)
                .from(USERS)
                .where(searchCondition)
                .orderBy(USERS.ID.desc())
                .limit(limit)
                .offset(offset)
                .fetchInto(UserResponse.class);
    }

    @Override
    public long countAll(String searchText) {
        Condition searchCondition = buildSearchCondition(searchText);
        return dsl.selectCount()
                .from(USERS)
                .where(searchCondition)
                .fetchOne(0, long.class);
    }

    private Condition buildSearchCondition(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return USERS.ID.isNotNull();
        }
        String pattern = "%" + searchText.trim() + "%";
        return USERS.NAME.likeIgnoreCase(pattern).or(USERS.EMAIL.likeIgnoreCase(pattern));
    }
}
