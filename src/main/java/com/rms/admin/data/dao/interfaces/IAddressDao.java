package com.rms.admin.data.dao.interfaces;

import com.rms.admin.data.dto.users.Address;

public interface IAddressDao {
    Long insert(String localAddress, String city, String state, String country);
    void deleteById(Long id);

    void update(Long id, Address address);
}