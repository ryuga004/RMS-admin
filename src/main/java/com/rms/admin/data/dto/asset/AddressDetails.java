package com.rms.admin.data.dto.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDetails {
    private String localAddress;
    private String city;
    private String state;
    private String country;
}