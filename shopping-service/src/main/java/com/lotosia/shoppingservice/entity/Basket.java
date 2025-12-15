package com.lotosia.shoppingservice.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
@Entity
@Table(name = "baskets")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Basket extends BaseAuditableEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "basket_products", joinColumns = @JoinColumn(name = "basket_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Long, Integer> productQuantities;
}
