package com.lotosia.profileservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author: nijataghayev
 */

@Entity
@Table(name = "reviews")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Review extends BaseAuditableEntity {

    private Long userId;
    private Long productId;
    private Long orderId;
    private String comment;

    @Size(min = 1, max = 5)
    private Integer rating;
    private LocalDateTime createdDate;
}
