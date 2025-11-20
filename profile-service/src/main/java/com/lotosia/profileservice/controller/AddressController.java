package com.lotosia.profileservice.controller;

import com.lotosia.profileservice.dto.address.AddressResponse;
import com.lotosia.profileservice.dto.address.CreateAddressRequest;
import com.lotosia.profileservice.dto.address.UpdateAddressRequest;
import com.lotosia.profileservice.service.AddressService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: nijataghayev
 */

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Address Controller")
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/create")
    public ResponseEntity<AddressResponse> create(@RequestBody @Valid CreateAddressRequest address) {
        return ResponseEntity.ok(addressService.create(address));
    }

    @PutMapping("/update")
    public ResponseEntity<AddressResponse> update(@RequestBody @Valid UpdateAddressRequest address) {
        return ResponseEntity.ok(addressService.update(address));
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<AddressResponse>> getByProfile(@PathVariable Long profileId) {
        List<AddressResponse> responses = addressService.getByProfile(profileId);

        if (responses.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {
        addressService.delete(id);
    }
}
