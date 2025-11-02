package com.example.bank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bank Transaction entity
 */
@Entity
@Table(name = "bank_transactions", schema = "bank", indexes = {
    @Index(name = "idx_bank_transactions_order_id", columnList = "order_id"),
    @Index(name = "idx_bank_transactions_status", columnList = "status"),
    @Index(name = "idx_bank_transactions_from_account", columnList = "from_account"),
    @Index(name = "idx_bank_transactions_to_account", columnList = "to_account"),
    @Index(name = "idx_bank_transactions_type", columnList = "type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankTransaction {

    @Id
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "from_account", length = 50, nullable = false)
    private String fromAccount;

    @Column(name = "to_account", length = 50, nullable = false)
    private String toAccount;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private TransactionStatus status;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Mark transaction as successful
     */
    public void markAsSuccess() {
        this.status = TransactionStatus.SUCCESS;
    }

    /**
     * Mark transaction as failed
     */
    public void markAsFailed() {
        this.status = TransactionStatus.FAILED;
    }

    /**
     * Check if transaction is successful
     */
    public boolean isSuccessful() {
        return this.status == TransactionStatus.SUCCESS;
    }

    /**
     * Check if transaction is failed
     */
    public boolean isFailed() {
        return this.status == TransactionStatus.FAILED;
    }

    /**
     * Check if transaction is pending
     */
    public boolean isPending() {
        return this.status == TransactionStatus.PENDING;
    }
}
