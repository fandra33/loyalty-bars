package com.loyalty.gateway.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rewards", indexes = {
    @Index(name = "idx_rewards_bar", columnList = "bar_id"),
    @Index(name = "idx_rewards_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    private Bar bar;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "points_cost", nullable = false)
    private Integer pointsCost;

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
    @OneToMany(mappedBy = "reward", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Redemption> redemptions = new HashSet<>();

    // Helper methods
    public boolean canBeRedeemedBy(User user) {
        return active && user.getPointsBalance() >= pointsCost;
    }

    public boolean belongsToBar(Bar bar) {
        return this.bar != null && this.bar.getId().equals(bar.getId());
    }
}
