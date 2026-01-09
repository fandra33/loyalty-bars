package com.loyalty.gateway.model.entity;

import com.loyalty.gateway.model.entity.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transactions_client", columnList = "client_id"),
    @Index(name = "idx_transactions_bar", columnList = "bar_id"),
    @Index(name = "idx_transactions_created", columnList = "created_at"),
    @Index(name = "idx_transactions_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bar_id", nullable = false)
    private Bar bar;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    private Redemption redemption;

    public static Integer calculatePoints(BigDecimal amount) {
        return amount.intValue();
    }

    public enum TransactionType {
        PURCHASE,
        REDEMPTION
    }

    public boolean isPurchase() {
        return type == TransactionType.PURCHASE;
    }

    public boolean isRedemption() {
        return type == TransactionType.REDEMPTION;
    }
}
