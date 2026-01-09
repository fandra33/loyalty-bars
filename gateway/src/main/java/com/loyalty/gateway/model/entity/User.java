package com.loyalty.gateway.model.entity;

import com.loyalty.gateway.model.entity.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_role", columnList = "role")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "points_balance", nullable = false)
    @Builder.Default
    private Integer pointsBalance = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Bar> ownedBars = new HashSet<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Transaction> transactions = new HashSet<>();

    @OneToMany(mappedBy = "usedBy", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<QRCode> usedQRCodes = new HashSet<>();

    public void addPoints(Integer points) {
        if (points < 0) {
            throw new IllegalArgumentException("Cannot add negative points");
        }
        this.pointsBalance += points;
    }

    public void deductPoints(Integer points) {
        if (points < 0) {
            throw new IllegalArgumentException("Cannot deduct negative points");
        }
        if (this.pointsBalance < points) {
            throw new IllegalStateException("Insufficient points balance");
        }
        this.pointsBalance -= points;
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return email;
    }

    public boolean isClient() {
        return role == UserRole.CLIENT;
    }

    public boolean isBarAdmin() {
        return role == UserRole.BAR_ADMIN;
    }
}
