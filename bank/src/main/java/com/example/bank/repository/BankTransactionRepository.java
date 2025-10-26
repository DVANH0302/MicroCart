package com.example.bank.repository;

import com.example.bank.entity.BankTransaction;
import com.example.bank.entity.TransactionStatus;
import com.example.bank.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BankTransaction entity
 */
@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, String> {

    /**
     * Find transaction by order ID and type
     */
    Optional<BankTransaction> findByOrderIdAndType(Integer orderId, TransactionType type);

    /**
     * Find successful transaction by order ID and type
     */
    @Query("SELECT t FROM BankTransaction t WHERE t.orderId = :orderId AND t.type = :type AND t.status = :status")
    Optional<BankTransaction> findByOrderIdAndTypeAndStatus(
        @Param("orderId") Integer orderId,
        @Param("type") TransactionType type,
        @Param("status") TransactionStatus status
    );

    /**
     * Check if transaction exists for order ID and type
     */
    boolean existsByOrderIdAndType(Integer orderId, TransactionType type);

    /**
     * Check if successful transaction exists for order ID and type
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM BankTransaction t WHERE t.orderId = :orderId AND t.type = :type AND t.status = 'SUCCESS'")
    boolean existsSuccessfulTransactionByOrderIdAndType(
        @Param("orderId") Integer orderId,
        @Param("type") TransactionType type
    );

    /**
     * Find all transactions for a specific account
     */
    @Query("SELECT t FROM BankTransaction t WHERE t.fromAccount = :accountId OR t.toAccount = :accountId ORDER BY t.createdAt DESC")
    List<BankTransaction> findAllByAccount(@Param("accountId") String accountId);

    /**
     * Find all transactions for a specific order
     */
    List<BankTransaction> findAllByOrderIdOrderByCreatedAtDesc(Integer orderId);

    /**
     * Find all transactions by status
     */
    List<BankTransaction> findAllByStatusOrderByCreatedAtDesc(TransactionStatus status);

    /**
     * Find all transactions by type
     */
    List<BankTransaction> findAllByTypeOrderByCreatedAtDesc(TransactionType type);
}
