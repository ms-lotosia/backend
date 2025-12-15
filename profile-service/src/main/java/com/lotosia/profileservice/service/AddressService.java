package com.lotosia.profileservice.service;

import com.lotosia.profileservice.dto.address.AddressResponse;
import com.lotosia.profileservice.dto.address.CreateAddressRequest;
import com.lotosia.profileservice.dto.address.UpdateAddressRequest;
import com.lotosia.profileservice.entity.Address;
import com.lotosia.profileservice.entity.Profile;
import com.lotosia.profileservice.exception.ResourceNotFoundException;
import com.lotosia.profileservice.repository.AddressRepository;
import com.lotosia.profileservice.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class AddressService {

    private final ProfileRepository profileRepository;
    private final AddressRepository addressRepository;

    public AddressResponse create(CreateAddressRequest request) {
        Profile profile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultAddress(profile.getId());
        }

        Address address = new Address();
        address.setProfile(profile);
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

        Address saved = addressRepository.save(address);
        return mapToDto(saved);
    }

    public AddressResponse update(UpdateAddressRequest request) {
        Address address = addressRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (request.getIsDefault() != null && request.getIsDefault()) {
            addressRepository.clearDefaultAddress(address.getProfile().getId());
        }

        if (request.getFirstName() != null) address.setFirstName(request.getFirstName());
        if (request.getLastName() != null) address.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) address.setPhoneNumber(request.getPhoneNumber());
        if (request.getAddressLine1() != null) address.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) address.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getState() != null) address.setState(request.getState());
        if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) address.setCountry(request.getCountry());
        if (request.getIsDefault() != null) address.setIsDefault(request.getIsDefault());

        Address saved = addressRepository.save(address);
        return mapToDto(saved);
    }

    public List<AddressResponse> getByProfile(Long profileId) {
        return addressRepository.findByProfileId(profileId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public void delete(Long id) {
        addressRepository.deleteById(id);
    }

    private AddressResponse mapToDto(Address a) {
        return new AddressResponse(
                a.getId(),
                a.getProfile().getId(),
                a.getFirstName(),
                a.getLastName(),
                a.getPhoneNumber(),
                a.getAddressLine1(),
                a.getAddressLine2(),
                a.getCity(),
                a.getState(),
                a.getPostalCode(),
                a.getCountry(),
                a.getIsDefault()
        );
    }
}
