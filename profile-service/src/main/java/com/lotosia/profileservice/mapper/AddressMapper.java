package com.lotosia.profileservice.mapper;

import com.lotosia.profileservice.dto.address.CreateAddressRequest;
import com.lotosia.profileservice.dto.address.AddressResponse;
import com.lotosia.profileservice.entity.Address;

/**
 * @author: nijataghayev
 */

public class AddressMapper {

    public static void updateEntity(Address address, CreateAddressRequest request) {
        if (request == null) {
            return;
        }

        address.setFirstName(request.getFirstName());
        address.setLastName(request.getLastName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setIsDefault(request.getIsDefault());
    }
}
