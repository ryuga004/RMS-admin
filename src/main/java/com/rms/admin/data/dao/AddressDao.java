package com.rms.admin.data.dao;

import com.rms.admin.data.dao.interfaces.IAddressDao;
import com.rms.admin.data.dto.users.Address;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.rms.admin.persistence.tables.Address.ADDRESS;

@Repository
@RequiredArgsConstructor
public class AddressDao implements IAddressDao {

    private final DSLContext dsl;

    @Override
    public Long insert(String localAddress, String city, String state, String country) {
        return dsl.insertInto(ADDRESS)
                .columns(ADDRESS.LOCAL_ADDRESS, ADDRESS.CITY, ADDRESS.STATE, ADDRESS.COUNTRY)
                .values(localAddress, city, state, country)
                .returningResult(ADDRESS.ID)
                .fetchOneInto(Long.class);
    }

    @Override
    public void deleteById(Long id) {
        dsl.deleteFrom(ADDRESS).where(ADDRESS.ID.eq(id)).execute();
    }

    @Override
    public void update(Long id, Address address) {
        dsl.update(ADDRESS)
                .set(ADDRESS.LOCAL_ADDRESS, address.getLocalAddress())
                .set(ADDRESS.CITY, address.getCity())
                .set(ADDRESS.COUNTRY, address.getCountry())
                .set(ADDRESS.STATE, address.getState())
                .where(ADDRESS.ID.eq(id))
                .execute();
    }
}
