package com.rms.admin.data.dto.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResult {
    private Long id;
    private String name;
    private String email;
    private Long roleId;
    private String roleName;
    private String phoneNo;
    private Boolean isVerified;
    private OffsetDateTime createdAt;
    private String imageKey;
    private String localAddress;
    private String city;
    private String state;
    private String country;
}
