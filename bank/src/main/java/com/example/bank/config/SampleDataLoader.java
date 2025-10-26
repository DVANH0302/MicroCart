package com.example.bank.config;

import com.example.bank.entity.BankAccount;
import com.example.bank.repository.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Sample data loader for development and testing
 * This will only insert data if it doesn't already exist
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SampleDataLoader {

    @Bean
    CommandLineRunner initBankData(BankAccountRepository accountRepository) {
        return args -> {
            log.info("Checking if sample bank data needs to be loaded...");

            // Only load if database is empty (data might come from SQL init script)
            if (accountRepository.count() > 0) {
                log.info("Bank accounts already exist. Skipping sample data loading.");
                return;
            }

            log.info("Loading sample bank account data...");

            // Create Store account
            BankAccount storeAccount = BankAccount.builder()
                .accountId("STORE_MAIN")
                .holderName("Online Store")
                .balance(new BigDecimal("10000.00"))
                .build();
            accountRepository.save(storeAccount);

            // Create customer accounts
            BankAccount cust1 = BankAccount.builder()
                .accountId("CUST_001")
                .holderName("User 1")
                .balance(new BigDecimal("5000.00"))
                .build();
            accountRepository.save(cust1);

            BankAccount cust2 = BankAccount.builder()
                .accountId("CUST_002")
                .holderName("User 2")
                .balance(new BigDecimal("3000.00"))
                .build();
            accountRepository.save(cust2);

            BankAccount cust3 = BankAccount.builder()
                .accountId("CUST_003")
                .holderName("User 3")
                .balance(new BigDecimal("7500.00"))
                .build();
            accountRepository.save(cust3);

            BankAccount cust4 = BankAccount.builder()
                .accountId("CUST_004")
                .holderName("User 4")
                .balance(new BigDecimal("1000.00"))
                .build();
            accountRepository.save(cust4);

            // Account with low balance for testing insufficient funds
            BankAccount cust5 = BankAccount.builder()
                .accountId("CUST_005")
                .holderName("User 5")
                .balance(new BigDecimal("500.00"))
                .build();
            accountRepository.save(cust5);

            log.info("Sample bank account data loaded successfully");
            log.info("Total accounts created: {}", accountRepository.count());
        };
    }
}
