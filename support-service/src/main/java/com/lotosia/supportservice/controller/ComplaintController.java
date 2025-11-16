package com.lotosia.supportservice.controller;

import com.lotosia.supportservice.dto.ComplaintRequest;
import com.lotosia.supportservice.dto.ComplaintResponse;
import com.lotosia.supportservice.service.ComplaintService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: nijataghayev
 */

@RestController
@RequestMapping("/api/v1/complaints")
@Tag(name = "Complaint Controller")
@RequiredArgsConstructor
public class ComplaintController {
    private final ComplaintService complaintService;

    @Operation(summary = "Create a new complaint")
    @PostMapping("/create")
    public ResponseEntity<ComplaintResponse> createComplaint(
            @Valid @RequestBody ComplaintRequest complaintRequestDto) {
        ComplaintResponse createdComplaint = complaintService.createComplaint(complaintRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdComplaint);
    }

    @Operation(summary = "Find all complaints pageable")
    @GetMapping("/getAll")
    public ResponseEntity<Page<ComplaintResponse>> getAllComplaints(
            @ParameterObject @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<ComplaintResponse> complaintPage = complaintService.getAllComplaints(pageable);
        return ResponseEntity.ok(complaintPage);
    }
}
