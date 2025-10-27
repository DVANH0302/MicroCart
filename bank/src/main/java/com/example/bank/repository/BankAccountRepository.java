package com.example.bank.repository;

import com.example.bank.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Repository for BankAccount entity
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    /**
     * Find account by ID with pessimistic write lock
     * This prevents concurrent modifications to the same account
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM BankAccount a WHERE a.accountId = :accountId")
    Optional<BankAccount> findByIdWithLock(@Param("accountId") String accountId);

    /**
     * Check if account exists by ID
     */
    boolean existsByAccountId(String accountId);

    /**
     * Find account by holder name
     */
    Optional<BankAccount> findByHolderName(String holderName);
}
