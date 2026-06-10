package com.rms.admin.data.dto.users.profile;

import com.rms.admin.data.dto.users.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileUpdateRequest {
    private Address address;
    private String phoneNo;
}