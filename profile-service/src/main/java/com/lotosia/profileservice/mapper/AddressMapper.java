package com.lotosia.profileservice.mapper;

import com.lotosia.profileservice.dto.address.AddressRequest;
import com.lotosia.profileservice.dto.address.AddressResponse;
import com.lotosia.profileservice.entity.Address;

/**
 * @author: nijataghayev
 */

public class AddressMapper {

    public static Address mapToEntity(AddressRequest addressRequest) {
        Address address = new Address();
        updateEntity(address, addressRequest);
        return address;
    }

    public static void updateEntity(Address address, AddressRequest request) {
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

    public static AddressResponse mapToDto(Address address) {
        if (address == null) {
            return null;
        }

        return AddressResponse.builder()
                .id(address.getId())
                .firstName(address.getFirstName())
                .lastName(address.getLastName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .build();
    }
}
