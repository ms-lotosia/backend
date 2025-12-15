package com.lotosia.supportservice.service;

import com.lotosia.supportservice.dto.ComplaintRequest;
import com.lotosia.supportservice.dto.ComplaintResponse;
import com.lotosia.supportservice.entity.Complaint;
import com.lotosia.supportservice.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ComplaintService {
    private final ComplaintRepository complaintRepository;
    private final EmailService emailService;

    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request) {
        Complaint complaint = mapToEntity(request);
        Complaint saved = complaintRepository.save(complaint);

        try {
            emailService.sendComplaintAcknowledgement(saved.getEmail());
        } catch (Exception e) {

        }

        try {
            emailService.sendNewComplaintNotificationToTeam(saved);
        } catch (Exception e) {

        }

        return mapToDto(saved);
    }

    public Page<ComplaintResponse> getAllComplaints(Pageable pageable) {
        Page<Complaint> complaints = complaintRepository.findAll(pageable);

        if (!complaints.hasContent()) {

        } else {

        }

        List<ComplaintResponse> dtos = complaints.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, complaints.getTotalElements());
    }

    private Complaint mapToEntity(ComplaintRequest request) {
        Complaint complaint = new Complaint();
        complaint.setEmail(request.getEmail());
        complaint.setMessage(request.getMessage());
        return complaint;
    }

    private ComplaintResponse mapToDto(Complaint complaint) {
        ComplaintResponse dto = new ComplaintResponse();
        dto.setId(complaint.getId());
        dto.setEmail(complaint.getEmail());
        dto.setMessage(complaint.getMessage());
        dto.setCreatedAt(complaint.getCreatedAt());

        return dto;
    }
}
