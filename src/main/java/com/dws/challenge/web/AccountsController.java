package com.dws.challenge.web;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/v1/accounts")
@Slf4j
@RequiredArgsConstructor
public class AccountsController {

  private final AccountsService accountsService;


  @Operation(summary = "Create a new account")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "201", description = "Account created"),
          @ApiResponse(responseCode = "400", description = "Duplicate account ID or invalid input")
  })
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
    log.info("Creating account {}", account);

    try {
      this.accountsService.createAccount(account);
    } catch (DuplicateAccountIdException daie) {
      return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }


  @Operation(summary = "Get account details by ID")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Account found"),
          @ApiResponse(responseCode = "404", description = "Account not found")
  })
  @GetMapping(path = "/{accountId}")
  public Account getAccount(@PathVariable String accountId) {
    log.info("Retrieving account for id {}", accountId);
    return this.accountsService.getAccount(accountId);
  }


  @Operation(summary = "Transfer funds between accounts")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Transfer successful"),
          @ApiResponse(responseCode = "400", description = "Invalid transfer request")
  })
  @PostMapping(path = "/transfer", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> transfer(@RequestBody @Valid TransferRequest request) {
    try {
      accountsService.transfer(request.getAccountFromId(), request.getAccountToId(), request.getAmount());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (IllegalArgumentException ex) {
      return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

}
