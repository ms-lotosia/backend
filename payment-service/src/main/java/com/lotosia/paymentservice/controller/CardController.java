package com.lotosia.paymentservice.controller;

import com.lotosia.paymentservice.dto.CardRequest;
import com.lotosia.paymentservice.dto.CardResponse;
import com.lotosia.paymentservice.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: nijataghayev
 */

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Card Controller")
public class CardController {

    private final CardService cardService;

    @Operation(summary = "Add a new card")
    @PostMapping
    public ResponseEntity<CardResponse> addCard(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CardRequest request) {
        CardResponse response = cardService.addCard(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all user cards")
    @GetMapping
    public ResponseEntity<List<CardResponse>> getUserCards(@RequestHeader("X-User-Id") Long userId) {
        List<CardResponse> cards = cardService.getUserCards(userId);
        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Get card by ID")
    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponse> getCard(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cardId) {
        CardResponse card = cardService.getCard(userId, cardId);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Set card as default")
    @PutMapping("/{cardId}/set-default")
    public ResponseEntity<CardResponse> setDefaultCard(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cardId) {
        CardResponse card = cardService.setDefaultCard(userId, cardId);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Delete a card")
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long cardId) {
        cardService.deleteCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }
}


