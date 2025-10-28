package com.example.bank.service.impl;

import com.example.bank.dto.request.CreateAccountRequest;
import com.example.bank.dto.request.DepositRequest;
import com.example.bank.dto.request.PaymentRequest;
import com.example.bank.dto.request.RefundRequest;
import com.example.bank.dto.request.WithdrawRequest;
import com.example.bank.dto.response.AccountBalanceResponse;
import com.example.bank.dto.response.CreateAccountResponse;
import com.example.bank.dto.response.DepositResponse;
import com.example.bank.dto.response.PaymentResponse;
import com.example.bank.dto.response.RefundResponse;
import com.example.bank.dto.response.TransactionResponse;
import com.example.bank.dto.response.WithdrawResponse;
import com.example.bank.entity.BankAccount;
import com.example.bank.entity.BankTransaction;
import com.example.bank.entity.TransactionStatus;
import com.example.bank.entity.TransactionType;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.DuplicateTransactionException;
import com.example.bank.exception.InsufficientFundsException;
import com.example.bank.exception.InvalidTransactionException;
import com.example.bank.repository.BankAccountRepository;
import com.example.bank.repository.BankTransactionRepository;
import com.example.bank.service.BankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of BankService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankServiceImpl implements BankService {

    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for order {}: {} from {} to {}",
            request.getOrderId(), request.getAmount(), request.getFromAccount(), request.getToAccount());

        // Check for existing successful payment for this order
        Optional<BankTransaction> existingPayment = transactionRepository
            .findByOrderIdAndTypeAndStatus(request.getOrderId(), TransactionType.PAYMENT, TransactionStatus.SUCCESS);

        if (existingPayment.isPresent()) {
            log.warn("Payment already exists for order {}: {}", request.getOrderId(), existingPayment.get().getTransactionId());
            // Return existing transaction to prevent duplicate payment (idempotency)
            BankTransaction existing = existingPayment.get();
            return PaymentResponse.builder()
                .transactionId(existing.getTransactionId())
                .status(existing.getStatus())
                .message("Payment already processed for this order")
                .amount(existing.getAmount())
                .orderId(existing.getOrderId())
                .timestamp(existing.getCreatedAt())
                .build();
        }

        // Generate transaction ID
        String transactionId = generateTransactionId();

        // Create transaction record with PENDING status
        BankTransaction transaction = BankTransaction.builder()
            .transactionId(transactionId)
            .fromAccount(request.getFromAccount())
            .toAccount(request.getToAccount())
            .amount(request.getAmount())
            .type(TransactionType.PAYMENT)
            .status(TransactionStatus.PENDING)
            .orderId(request.getOrderId())
            .build();

        try {
            // Fetch accounts with pessimistic lock to prevent concurrent modifications
            BankAccount fromAccount = accountRepository.findByIdWithLock(request.getFromAccount())
                .orElseThrow(() -> new AccountNotFoundException(request.getFromAccount(), true));

            BankAccount toAccount = accountRepository.findByIdWithLock(request.getToAccount())
                .orElseThrow(() -> new AccountNotFoundException(request.getToAccount(), false));

            // Validate sufficient balance
            if (!fromAccount.hasSufficientBalance(request.getAmount())) {
                log.warn("Insufficient funds for payment. Account: {}, Required: {}, Available: {}",
                    fromAccount.getAccountId(), request.getAmount(), fromAccount.getBalance());
                transaction.markAsFailed();
                transactionRepository.save(transaction);
                throw new InsufficientFundsException(
                    fromAccount.getAccountId(),
                    request.getAmount(),
                    fromAccount.getBalance()
                );
            }

            // Perform the transfer
            fromAccount.debit(request.getAmount());
            toAccount.credit(request.getAmount());

            // Save updated accounts
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Mark transaction as successful
            transaction.markAsSuccess();
            transactionRepository.save(transaction);

            log.info("Payment processed successfully. Transaction ID: {}, Order: {}",
                transactionId, request.getOrderId());

            return PaymentResponse.success(transactionId, request.getAmount(), request.getOrderId());

        } catch (AccountNotFoundException | InsufficientFundsException e) {
            // These exceptions are already handled above
            throw e;
        } catch (Exception e) {
            log.error("Error processing payment for order {}: ", request.getOrderId(), e);
            transaction.markAsFailed();
            transactionRepository.save(transaction);
            throw new InvalidTransactionException("Failed to process payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public RefundResponse processRefund(RefundRequest request) {
        log.info("Processing refund for order {}: {} (Original transaction: {})",
            request.getOrderId(), request.getAmount(), request.getOriginalTransactionId());

        // Find original payment transaction
        BankTransaction originalTransaction = transactionRepository
            .findById(request.getOriginalTransactionId())
            .orElseThrow(() -> new InvalidTransactionException(
                "Original transaction not found: " + request.getOriginalTransactionId()));

        // Validate original transaction
        if (!originalTransaction.isSuccessful()) {
            log.warn("Cannot refund unsuccessful transaction: {}", request.getOriginalTransactionId());
            throw new InvalidTransactionException(
                "Original transaction was not successful. Status: " + originalTransaction.getStatus());
        }

        if (originalTransaction.getType() != TransactionType.PAYMENT) {
            log.warn("Cannot refund non-payment transaction: {}", request.getOriginalTransactionId());
            throw new InvalidTransactionException(
                "Can only refund PAYMENT transactions. Type: " + originalTransaction.getType());
        }

        // Check if refund already exists for this order
        if (transactionRepository.existsSuccessfulTransactionByOrderIdAndType(
            request.getOrderId(), TransactionType.REFUND)) {
            log.warn("Refund already exists for order {}", request.getOrderId());
            throw new DuplicateTransactionException(request.getOrderId(), "REFUND");
        }

        // Validate refund amount doesn't exceed original payment
        if (request.getAmount().compareTo(originalTransaction.getAmount()) > 0) {
            throw new InvalidTransactionException(
                "Refund amount cannot exceed original payment amount");
        }

        // Generate refund transaction ID
        String refundTransactionId = generateTransactionId();

        // Create refund transaction (reverse direction)
        BankTransaction refundTransaction = BankTransaction.builder()
            .transactionId(refundTransactionId)
            .fromAccount(originalTransaction.getToAccount())  // Store account
            .toAccount(originalTransaction.getFromAccount())  // Customer account
            .amount(request.getAmount())
            .type(TransactionType.REFUND)
            .status(TransactionStatus.PENDING)
            .orderId(request.getOrderId())
            .build();

        try {
            // Fetch accounts with pessimistic lock
            BankAccount fromAccount = accountRepository.findByIdWithLock(refundTransaction.getFromAccount())
                .orElseThrow(() -> new AccountNotFoundException(refundTransaction.getFromAccount(), true));

            BankAccount toAccount = accountRepository.findByIdWithLock(refundTransaction.getToAccount())
                .orElseThrow(() -> new AccountNotFoundException(refundTransaction.getToAccount(), false));

            // Validate store account has sufficient balance for refund
            if (!fromAccount.hasSufficientBalance(request.getAmount())) {
                log.error("Store account has insufficient funds for refund. Required: {}, Available: {}",
                    request.getAmount(), fromAccount.getBalance());
                refundTransaction.markAsFailed();
                transactionRepository.save(refundTransaction);
                throw new InsufficientFundsException(
                    fromAccount.getAccountId(),
                    request.getAmount(),
                    fromAccount.getBalance()
                );
            }

            // Perform the refund transfer
            fromAccount.debit(request.getAmount());
            toAccount.credit(request.getAmount());

            // Save updated accounts
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // Mark refund transaction as successful
            refundTransaction.markAsSuccess();
            transactionRepository.save(refundTransaction);

            log.info("Refund processed successfully. Transaction ID: {}, Order: {}",
                refundTransactionId, request.getOrderId());

            return RefundResponse.success(
                refundTransactionId,
                request.getOriginalTransactionId(),
                request.getAmount(),
                request.getOrderId()
            );

        } catch (AccountNotFoundException | InsufficientFundsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing refund for order {}: ", request.getOrderId(), e);
            refundTransaction.markAsFailed();
            transactionRepository.save(refundTransaction);
            throw new InvalidTransactionException("Failed to process refund: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {
        log.info("Fetching transaction: {}", transactionId);

        BankTransaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new InvalidTransactionException("Transaction not found: " + transactionId));

        return mapToTransactionResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByOrderId(Integer orderId) {
        log.info("Fetching transactions for order: {}", orderId);

        List<BankTransaction> transactions = transactionRepository
            .findAllByOrderIdOrderByCreatedAtDesc(orderId);

        return transactions.stream()
            .map(this::mapToTransactionResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceResponse getAccountBalance(String accountId) {
        log.info("Fetching balance for account: {}", accountId);

        BankAccount account = accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));

        return AccountBalanceResponse.builder()
            .accountId(account.getAccountId())
            .holderName(account.getHolderName())
            .balance(account.getBalance())
            .timestamp(LocalDateTime.now())
            .build();
    }

    @Override
    @Transactional
    public CreateAccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating new account: {} for holder: {}", request.getAccountId(), request.getHolderName());

        // Check if account already exists
        if (accountRepository.existsById(request.getAccountId())) {
            log.warn("Account already exists: {}", request.getAccountId());
            throw new DuplicateTransactionException(
                "Account with ID " + request.getAccountId() + " already exists");
        }

        // Create new account
        BankAccount newAccount = BankAccount.builder()
            .accountId(request.getAccountId())
            .holderName(request.getHolderName())
            .balance(request.getInitialBalance())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        // Save to database
        BankAccount savedAccount = accountRepository.save(newAccount);

        log.info("Account created successfully: {}", savedAccount.getAccountId());

        return CreateAccountResponse.success(
            savedAccount.getAccountId(),
            savedAccount.getHolderName(),
            savedAccount.getBalance(),
            savedAccount.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public DepositResponse deposit(DepositRequest request) {
        log.info("Processing deposit: {} to account {}", request.getAmount(), request.getAccountId());

        // Fetch account with pessimistic lock to prevent concurrent modifications
        BankAccount account = accountRepository.findByIdWithLock(request.getAccountId())
            .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        // Credit the amount to the account
        account.credit(request.getAmount());

        // Update the account
        BankAccount updatedAccount = accountRepository.save(account);

        log.info("Deposit successful. Account: {}, New balance: {}",
            account.getAccountId(), updatedAccount.getBalance());

        return DepositResponse.success(
            updatedAccount.getAccountId(),
            request.getAmount(),
            updatedAccount.getBalance()
        );
    }

    @Override
    @Transactional
    public WithdrawResponse withdraw(WithdrawRequest request) {
        log.info("Processing withdrawal: {} from account {}", request.getAmount(), request.getAccountId());

        // Fetch account with pessimistic lock to prevent concurrent modifications
        BankAccount account = accountRepository.findByIdWithLock(request.getAccountId())
            .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));

        // Check sufficient balance
        if (!account.hasSufficientBalance(request.getAmount())) {
            log.warn("Insufficient funds for withdrawal. Account: {}, Required: {}, Available: {}",
                account.getAccountId(), request.getAmount(), account.getBalance());
            throw new InsufficientFundsException(
                account.getAccountId(),
                request.getAmount(),
                account.getBalance()
            );
        }

        // Debit the amount from the account
        account.debit(request.getAmount());

        // Update the account
        BankAccount updatedAccount = accountRepository.save(account);

        log.info("Withdrawal successful. Account: {}, New balance: {}",
            account.getAccountId(), updatedAccount.getBalance());

        return WithdrawResponse.success(
            updatedAccount.getAccountId(),
            request.getAmount(),
            updatedAccount.getBalance()
        );
    }

    /**
     * Generate unique transaction ID
     */
    private String generateTransactionId() {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN_" + timestamp + "_" + uuid;
    }

    /**
     * Map BankTransaction entity to TransactionResponse DTO
     */
    private TransactionResponse mapToTransactionResponse(BankTransaction transaction) {
        return TransactionResponse.builder()
            .transactionId(transaction.getTransactionId())
            .fromAccount(transaction.getFromAccount())
            .toAccount(transaction.getToAccount())
            .amount(transaction.getAmount())
            .type(transaction.getType())
            .status(transaction.getStatus())
            .orderId(transaction.getOrderId())
            .createdAt(transaction.getCreatedAt())
            .updatedAt(transaction.getUpdatedAt())
            .build();
    }
}
