package com.lotosia.contentservice.controller;

import com.lotosia.contentservice.dto.ContactUsRequest;
import com.lotosia.contentservice.dto.ContactUsResponse;
import com.lotosia.contentservice.service.ContactUsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/v1/contactUs")
@Tag(name = "Contact us Controller")
@RequiredArgsConstructor
public class ContactUsController {
    private final ContactUsService contactUsService;

    @Operation(summary = "Create a new contact us")
    @PostMapping
    public ResponseEntity<ContactUsResponse> createContactUs(@Valid @RequestBody ContactUsRequest contactUs) {
        ContactUsResponse dto = contactUsService.createContactUs(contactUs);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Operation(summary = "Find contact us with ID")
    @GetMapping("/{id}")
    public ContactUsResponse findContactUs(@PathVariable Long id) {
        return contactUsService.getContactUs(id);
    }

    @Operation(summary = "Find all contact us pageable")
    @GetMapping("/all")
    public ResponseEntity<Page<ContactUsResponse>> findAllContactUs(@ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<ContactUsResponse> contactUsPage = contactUsService.getAllContactUs(pageable);
        return ResponseEntity.ok(contactUsPage);
    }
}
