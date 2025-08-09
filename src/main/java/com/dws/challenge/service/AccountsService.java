package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  private final NotificationService notificationService;


  /**
   * Creates a new account.
   *
   * @param account the Account object to create
   * @throws DuplicateAccountIdException if an account with the same ID already exists
   */
  public void createAccount(Account account) {
    log.info("Creating account with ID: {}", account.getAccountId());
    this.accountsRepository.createAccount(account);
  }


  /**
   * Retrieves the account with the specified ID.
   *
   * @param accountId the ID of the account to retrieve
   * @return the Account object if found, or null if not found
   */
  public Account getAccount(String accountId) {
    log.info("Retrieving account with ID: {}", accountId);
    return this.accountsRepository.getAccount(accountId);
  }


  /**
   * Transfers the specified amount from one account to another.
   *
   * @param fromId   the ID of the account to transfer from
   * @param toId     the ID of the account to transfer to
   * @param amount   the amount to transfer
   * @throws IllegalArgumentException if the transfer is invalid (e.g., same account, null amount, insufficient funds)
   */
  public void transfer(String fromId, String toId, BigDecimal amount) {

    log.info("Initiating transfer of {} from {} to {}", amount, fromId, toId);

    if (fromId.equals(toId)) {
      log.error("Transfer failed: source and destination account are the same ({})", fromId);
      throw new IllegalArgumentException("Cannot transfer to the same account.");
    }

    Objects.requireNonNull(amount, "Transfer amount must not be null.");

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      log.error("Transfer failed: amount must be positive. Provided: {}", amount);
      throw new IllegalArgumentException("Transfer amount must be positive.");
    }

    Account from = accountsRepository.getAccount(fromId);
    Account to = accountsRepository.getAccount(toId);

    if (from == null || to == null) {
      log.error("Transfer failed: Account not found. From: {}, To: {}", fromId, toId);
      throw new IllegalArgumentException("Account not found.");
    }

    if (from.getBalance().compareTo(amount) < 0) {
      log.error("Transfer failed: Insufficient funds in account {}", fromId);
      throw new IllegalArgumentException("Insufficient funds.");
    }

    Account first = fromId.compareTo(toId) < 0 ? from : to;
    Account second = fromId.compareTo(toId) < 0 ? to : from;

    lockBothAccounts(first, second, () -> {
      from.setBalance(from.getBalance().subtract(amount));
      to.setBalance(to.getBalance().add(amount));
      log.info("Transfer completed: {} from {} to {}", amount, fromId, toId);
    });

    notifyTransfer(from, to, amount, fromId, toId);
  }


  /**
   * Locks both accounts to ensure thread-safe transfer operations.
   *
   * @param first  the first account to lock
   * @param second the second account to lock
   * @param action the action to perform while both accounts are locked
   */
  private void lockBothAccounts(Account first, Account second, Runnable action) {
    first.getLock().lock();
    try {
      second.getLock().lock();
      try {
        action.run();
      } finally {
        second.getLock().unlock();
      }
    } finally {
      first.getLock().unlock();
    }
  }

  /**
   * Notifies both accounts about the transfer.
   *
   * @param from   the account transferring the amount
   * @param to     the account receiving the amount
   * @param amount the amount transferred
   * @param fromId the ID of the account transferring the amount
   * @param toId   the ID of the account receiving the amount
   */
  private void notifyTransfer(Account from, Account to, BigDecimal amount, String fromId, String toId) {

    notificationService.notifyAboutTransfer(from, "Transferred " + amount + " to account " + toId);
    notificationService.notifyAboutTransfer(to, "Received " + amount + " from account " + fromId);
    log.info("Notifications sent for transfer of {} from {} to {}", amount, fromId, toId);
  }
}
