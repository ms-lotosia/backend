package com.lotosia.contentservice.service;

import com.lotosia.contentservice.dto.ContactUsRequest;
import com.lotosia.contentservice.dto.ContactUsResponse;
import com.lotosia.contentservice.entity.ContactUs;
import com.lotosia.contentservice.exception.ResourceNotFoundException;
import com.lotosia.contentservice.repository.ContactUsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
public class ContactUsService {

    private final ContactUsRepository contactUsRepository;
    private final EmailService emailService;

    @Transactional
    public ContactUsResponse createContactUs(ContactUsRequest contactUsRequest) {
        ContactUs contactUs = mapToEntity(contactUsRequest);
        ContactUs savedContactUs = contactUsRepository.save(contactUs);

        try {
            emailService.sendContactUsAcknowledgement(savedContactUs.getEmail(), contactUs.getFirstName());
        } catch (Exception e) {
        }

        try {
            emailService.sendNewContactUsNotificationToTeam(savedContactUs);
        } catch (Exception e) {
        }

        return mapToDto(savedContactUs);
    }

    public Page<ContactUsResponse> getAllContactUs(Pageable pageable) {
        Page<ContactUs> contactUsPage = contactUsRepository.findAll(pageable);

        if (!contactUsPage.hasContent()) {

        } else {

        }

        List<ContactUsResponse> dtos = contactUsPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, contactUsPage.getTotalElements());
    }

    public ContactUsResponse getContactUs(Long id) {
        ContactUs contactUs = contactUsRepository.findById(id)
                .orElseThrow(() -> {

                    String message = String.format("ContactUs not found with id: %d", id);
                    return new ResourceNotFoundException(message);
                });

        return mapToDto(contactUs);
    }

    private ContactUs mapToEntity(ContactUsRequest contactUsRequest) {
        ContactUs contactUs = new ContactUs();
        contactUs.setEmail(contactUsRequest.getEmail());
        contactUs.setFirstName(contactUsRequest.getFirstName());
        contactUs.setLastName(contactUsRequest.getLastName());
        contactUs.setSubject(contactUsRequest.getSubject());
        contactUs.setMessage(contactUsRequest.getMessage());
        return contactUs;
    }

    private ContactUsResponse mapToDto(ContactUs contactUs) {
        ContactUsResponse dto = new ContactUsResponse();
        dto.setId(contactUs.getId());
        dto.setEmail(contactUs.getEmail());
        dto.setFirstName(contactUs.getFirstName());
        dto.setLastName(contactUs.getLastName());
        dto.setSubject(contactUs.getSubject());
        dto.setMessage(contactUs.getMessage());
        dto.setCreatedAt(contactUs.getCreatedAt());
        return dto;
    }
}
