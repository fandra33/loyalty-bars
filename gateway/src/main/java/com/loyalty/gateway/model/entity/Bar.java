package com.loyalty.gateway.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bars", indexes = {
    @Index(name = "idx_bars_owner", columnList = "owner_id"),
    @Index(name = "idx_bars_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String address;

    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relații
    @OneToMany(mappedBy = "bar", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Reward> rewards = new HashSet<>();

    @OneToMany(mappedBy = "bar", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Transaction> transactions = new HashSet<>();

    @OneToMany(mappedBy = "bar", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<QRCode> qrCodes = new HashSet<>();

    // Helper methods
    public void addReward(Reward reward) {
        rewards.add(reward);
        reward.setBar(this);
    }

    public void removeReward(Reward reward) {
        rewards.remove(reward);
        reward.setBar(null);
    }

    public boolean isOwnedBy(User user) {
        return owner != null && owner.getId().equals(user.getId());
    }
}
