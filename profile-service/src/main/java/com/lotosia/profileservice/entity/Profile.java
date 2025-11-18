package com.lotosia.profileservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * @author: nijataghayev
 */

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends BaseAuditableEntity {

    private Long userId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    private List<Address> addresses;

    @OneToOne(mappedBy = "profile", orphanRemoval = true, cascade = CascadeType.ALL)
    private UserPreference userPreference;
}
