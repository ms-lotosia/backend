package com.lotosia.contentservice.service;

import com.lotosia.contentservice.criteria.PageCriteria;
import com.lotosia.contentservice.dto.FAQRequest;
import com.lotosia.contentservice.dto.FAQResponse;
import com.lotosia.contentservice.dto.PageableResponse;
import com.lotosia.contentservice.entity.FAQ;
import com.lotosia.contentservice.exception.BadRequestException;
import com.lotosia.contentservice.exception.NotFoundException;
import com.lotosia.contentservice.repository.FAQRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class FAQService {
    private final FAQRepository faqRepository;

    public PageableResponse<FAQResponse> getAllFAQs(PageCriteria pageCriteria) {
        PageRequest pageable = PageRequest.of(pageCriteria.getPage(),
                pageCriteria.getCount(),
                Sort.by("id").ascending());

        Page<FAQ> faqs = faqRepository.findAll(pageable);

        return toPageableFAQResponse(faqs);
    }

    public FAQResponse getFAQById(Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException(
                    "INVALID_FAQ_ID",
                    String.format("FAQ ID must be positive. Provided: %s", id));
        }

        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FAQ_NOT_FOUND",
                        String.format("FAQ with ID %s not found", id)));

        return mapToDto(faq);
    }

    public FAQ createFAQ(FAQRequest dto) {
        FAQ faq = mapToEntity(dto);
        return faqRepository.save(faq);
    }

    public void updateFAQ(Long id, FAQRequest dto){
        FAQ faq = faqRepository.findById(id).orElseThrow(
                () -> new NotFoundException(
                "FAQ_NOT_FOUND",
                String.format("FAQ with ID %s not found", id)));

        faq.setQuestion(dto.getQuestion());
        faq.setAnswer(dto.getAnswer());

        faqRepository.save(faq);
    }

    public void deleteFAQ(Long id){
        FAQ faq = faqRepository.findById(id).orElseThrow(
                () -> new NotFoundException(
                        "FAQ_NOT_FOUND",
                        String.format("FAQ with ID %s not found", id)));

        faqRepository.delete(faq);
    }

    private PageableResponse<FAQResponse> toPageableFAQResponse(Page<FAQ> faqs) {
        PageableResponse<FAQResponse> pageableResponse = new PageableResponse();
        pageableResponse.setTotalElements(faqs.getTotalElements());
        pageableResponse.setData(faqs.stream().map(this::mapToDto).toList());
        pageableResponse.setHasNextPage(faqs.hasNext());
        pageableResponse.setLastPageNumber(faqs.getNumber());

        return pageableResponse;
    }

    private FAQResponse mapToDto(FAQ faq) {
        FAQResponse dto = new FAQResponse();
        dto.setId(faq.getId());
        dto.setQuestion(faq.getQuestion());
        dto.setAnswer(faq.getAnswer());

        return dto;
    }

    private FAQ mapToEntity(FAQRequest faqRequest) {
        FAQ faq = new FAQ();
        faq.setQuestion(faqRequest.getQuestion());
        faq.setAnswer(faqRequest.getAnswer());

        return faq;
    }
}
