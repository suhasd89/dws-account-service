package com.dws.challenge.service;


import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

    @Autowired
    private AccountsService accountsService;

    @BeforeEach
    void setUp() {
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    void addAccount() {
        Account account = new Account("Id-123");
        account.setBalance(new BigDecimal(1000));
        this.accountsService.createAccount(account);

        assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
    }

    @Test
    void addAccount_failsOnDuplicateId() {
        String uniqueId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueId);
        this.accountsService.createAccount(account);

        try {
            this.accountsService.createAccount(account);
            fail("Should have failed when adding duplicate account");
        } catch (DuplicateAccountIdException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
        }
    }


    @Test
    void transferSuccess() {
        Account from = new Account("Id-1");
        from.setBalance(new BigDecimal("1000"));
        Account to = new Account("Id-2");
        to.setBalance(new BigDecimal("500"));
        accountsService.createAccount(from);
        accountsService.createAccount(to);

        accountsService.transfer("Id-1", "Id-2", new BigDecimal("200"));

        assertThat(accountsService.getAccount("Id-1").getBalance()).isEqualByComparingTo("800");
        assertThat(accountsService.getAccount("Id-2").getBalance()).isEqualByComparingTo("700");
    }

    @Test
    void transferFailsOnSameAccount() {
        Account account = new Account("Id-1");
        account.setBalance(new BigDecimal("1000"));
        accountsService.createAccount(account);

        try {
            accountsService.transfer("Id-1", "Id-1", new BigDecimal("100"));
            fail("Should have failed when transferring to the same account");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Cannot transfer to the same account.");
        }
    }

    @Test
    void transferFailsOnNegativeAmount() {
        Account from = new Account("Id-1");
        from.setBalance(new BigDecimal("1000"));
        Account to = new Account("Id-2");
        to.setBalance(new BigDecimal("500"));
        accountsService.createAccount(from);
        accountsService.createAccount(to);

        try {
            accountsService.transfer("Id-1", "Id-2", new BigDecimal("-100"));
            fail("Should have failed on negative amount");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Transfer amount must be positive.");
        }
    }

    @Test
    void transferFailsOnInsufficientFunds() {
        Account from = new Account("Id-1");
        from.setBalance(new BigDecimal("100"));
        Account to = new Account("Id-2");
        to.setBalance(new BigDecimal("500"));
        accountsService.createAccount(from);
        accountsService.createAccount(to);

        try {
            accountsService.transfer("Id-1", "Id-2", new BigDecimal("200"));
            fail("Should have failed on insufficient funds");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Insufficient funds.");
        }
    }

    @Test
    void transferFailsOnInvalidAccount() {
        Account from = new Account("Id-1");
        from.setBalance(new BigDecimal("1000"));
        accountsService.createAccount(from);

        try {
            accountsService.transfer("Id-1", "Id-999", new BigDecimal("100"));
            fail("Should have failed on invalid account");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Account not found.");
        }
    }
}
