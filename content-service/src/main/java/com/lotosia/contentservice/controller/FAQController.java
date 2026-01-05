package com.lotosia.contentservice.controller;

import com.lotosia.contentservice.criteria.PageCriteria;
import com.lotosia.contentservice.dto.FAQRequest;
import com.lotosia.contentservice.dto.FAQResponse;
import com.lotosia.contentservice.dto.PageableResponse;
import com.lotosia.contentservice.dto.ResponseModel;
import com.lotosia.contentservice.entity.FAQ;
import com.lotosia.contentservice.service.FAQService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
@RestController
@RequestMapping("/api/v1/faqs")
@RequiredArgsConstructor
@Tag(name = "FAQ Controller")
public class FAQController {
    private final FAQService faqService;

    @Operation(summary = "Get all FAQs")
    @GetMapping("/all")
    @ResponseStatus(OK)
    public PageableResponse<FAQResponse> getAll(@ModelAttribute PageCriteria pageCriteria) {
        return faqService.getAllFAQs(pageCriteria);
    }

    @Operation(summary = "Get FAQ by ID")
    @GetMapping("/{id}")
    public FAQResponse getById(@PathVariable Long id) {
        return faqService.getFAQById(id);
    }

    @Operation(summary = "Create a new FAQ")
    @PostMapping("/create")
    @ResponseStatus(CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public FAQResponse create(@Valid @RequestBody FAQRequest dto) {
        FAQ faq = faqService.createFAQ(dto);
        return new FAQResponse(faq.getId(), faq.getQuestion(), faq.getAnswer());
    }

    @Operation(summary = "Update FAQ by ID")
    @PutMapping("/update/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void update(@PathVariable Long id, @Valid @RequestBody FAQRequest dto) {
        faqService.updateFAQ(id, dto);
    }

    @Operation(summary = "Delete FAQ by ID")
    @DeleteMapping("/{id}")
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('ADMIN')")
    public void delete(@PathVariable Long id) {
        faqService.deleteFAQ(id);
    }
}
