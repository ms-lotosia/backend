package com.lotosia.supportservice.controller;

import com.lotosia.supportservice.criteria.PageCriteria;
import com.lotosia.supportservice.dto.PageableResponse;
import com.lotosia.supportservice.dto.ResponseModel;
import com.lotosia.supportservice.dto.SuggestionRequest;
import com.lotosia.supportservice.dto.SuggestionResponse;
import com.lotosia.supportservice.service.SuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

/**
 * @author: nijataghayev
 */

@RestController
@RequestMapping("/api/v1/suggestions")
@Tag(name = "Suggestion Controller")
@RequiredArgsConstructor
public class SuggestionController {
    private final SuggestionService suggestionService;

    @Operation(summary = "Create a new suggestion")
    @PostMapping("/submit")
    public ResponseEntity<SuggestionResponse> submit(@RequestBody SuggestionRequest dto) {
        SuggestionResponse response = suggestionService.createSuggestion(dto);

        return ResponseEntity.status(CREATED).body(response);
    }

    @Operation(summary = "Get all suggestions")
    @GetMapping("/all")
    public ResponseModel<PageableResponse<SuggestionResponse>> getAll(@ModelAttribute PageCriteria pageCriteria) {
        PageableResponse<SuggestionResponse> allSuggestions = suggestionService.getAllSuggestion(pageCriteria);
        return ResponseModel.<PageableResponse<SuggestionResponse>>builder()
                .data(allSuggestions)
                .build();
    }
}
