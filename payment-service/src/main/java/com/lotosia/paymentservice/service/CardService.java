package com.lotosia.paymentservice.service;

import com.lotosia.paymentservice.dto.CardRequest;
import com.lotosia.paymentservice.dto.CardResponse;
import com.lotosia.paymentservice.entity.Card;
import com.lotosia.paymentservice.exception.ResourceNotFoundException;
import com.lotosia.paymentservice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    @Transactional
    public CardResponse addCard(Long userId, CardRequest request) {
        if (cardRepository.existsByUserIdAndCardNumberAndIsActiveTrue(userId, request.getCardNumber())) {
            throw new IllegalArgumentException("Card with this number already exists");
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            cardRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId)
                    .ifPresent(card -> {
                        card.setIsDefault(false);
                        cardRepository.save(card);
                    });
        }

        Card card = mapToEntity(userId, request);
        Card saved = cardRepository.save(card);
        return mapToDto(saved);
    }

    public List<CardResponse> getUserCards(Long userId) {
        List<Card> cards = cardRepository.findByUserIdAndIsActiveTrue(userId);
        return cards.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CardResponse getCard(Long userId, Long cardId) {
        Card card = cardRepository.findByUserIdAndIdAndIsActiveTrue(userId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));
        return mapToDto(card);
    }

    @Transactional
    public CardResponse setDefaultCard(Long userId, Long cardId) {
        Card card = cardRepository.findByUserIdAndIdAndIsActiveTrue(userId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        cardRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId)
                .ifPresent(defaultCard -> {
                    defaultCard.setIsDefault(false);
                    cardRepository.save(defaultCard);
                });

        card.setIsDefault(true);
        Card saved = cardRepository.save(card);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteCard(Long userId, Long cardId) {
        Card card = cardRepository.findByUserIdAndIdAndIsActiveTrue(userId, cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        card.setIsActive(false);
        cardRepository.save(card);
    }

    private Card mapToEntity(Long userId, CardRequest request) {
        Card card = new Card();
        card.setUserId(userId);
        card.setCardNumber(request.getCardNumber());
        card.setCardHolderName(request.getCardHolderName());
        card.setExpiryMonth(request.getExpiryMonth());
        card.setExpiryYear(request.getExpiryYear());
        card.setCvv(request.getCvv());
        card.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        card.setIsActive(true);
        return card;
    }

    private CardResponse mapToDto(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setUserId(card.getUserId());
        response.setMaskedCardNumber(maskCardNumber(card.getCardNumber()));
        response.setCardHolderName(card.getCardHolderName());
        response.setExpiryMonth(card.getExpiryMonth());
        response.setExpiryYear(card.getExpiryYear());
        response.setIsDefault(card.getIsDefault());
        response.setIsActive(card.getIsActive());
        response.setCreatedDate(card.getCreatedDate());
        return response;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
