package com.lotosia.supportservice.service;

import com.lotosia.supportservice.criteria.PageCriteria;
import com.lotosia.supportservice.dto.PageableResponse;
import com.lotosia.supportservice.dto.SuggestionRequest;
import com.lotosia.supportservice.dto.SuggestionResponse;
import com.lotosia.supportservice.entity.Suggestion;
import com.lotosia.supportservice.repository.SuggestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: nijataghayev
 */

@Service
@RequiredArgsConstructor
public class SuggestionService {
    private final SuggestionRepository suggestionRepository;
    private final EmailService emailService;

    @Transactional
    public SuggestionResponse createSuggestion(SuggestionRequest dto) {
        Suggestion suggestion = mapToEntity(dto);
        Suggestion saved = suggestionRepository.save(suggestion);

        try {
            emailService.sendSuggestionAcknowledgement(saved.getEmail());
        } catch (Exception e) {

        }

        try {
            emailService.sendNewSuggestionNotificationToTeam(saved);
        } catch (Exception e) {

        }

        return mapToDto(saved);
    }

    public PageableResponse<SuggestionResponse> getAllSuggestion(PageCriteria pageCriteria) {
        PageRequest pageable = PageRequest.of(pageCriteria.getPage(),
                pageCriteria.getCount(),
                Sort.by("id").descending());
        Page<Suggestion> suggestionEntities = suggestionRepository.findAll(pageable);

        return toPageableSuggestionResponse(suggestionEntities);
    }

    private SuggestionResponse mapToDto(Suggestion suggestion) {
        SuggestionResponse dto = new SuggestionResponse();
        dto.setId(suggestion.getId());
        dto.setEmail(suggestion.getEmail());
        dto.setMessage(suggestion.getMessage());
        dto.setCreatedAt(suggestion.getCreatedAt());

        return dto;
    }

    private Suggestion mapToEntity(SuggestionRequest dto) {
        Suggestion suggestion = new Suggestion();
        suggestion.setEmail(dto.getEmail());
        suggestion.setMessage(dto.getMessage());

        return suggestion;
    }

    private PageableResponse<SuggestionResponse> toPageableSuggestionResponse(Page<Suggestion> suggestions) {
        PageableResponse<SuggestionResponse> pageableResponse = new PageableResponse<>();
        pageableResponse.setTotalElements(suggestions.getTotalElements());
        pageableResponse.setData(suggestions.stream().map(this::mapToDto).toList());
        pageableResponse.setHasNextPage(suggestions.hasNext());
        pageableResponse.setLastPageNumber(suggestions.getNumber());

        return pageableResponse;
    }
}
