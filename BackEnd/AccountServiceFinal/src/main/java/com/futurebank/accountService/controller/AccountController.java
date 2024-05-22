package com.futurebank.accountService.controller;

import java.util.List;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.futurebank.accountService.model.Account;
import com.futurebank.accountService.model.AccountCreationRequest;
import com.futurebank.accountService.model.Transaction;
import com.futurebank.accountService.model.TransferRequest;
import com.futurebank.accountService.service.AccountService;
import com.futurebank.accountService.service.TransactionService;
import com.futurebank.accountService.service.TransferService;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*") // Adjust the origins as per your requirements
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final TransferService transferService;

    public AccountController(AccountService accountService, TransactionService transactionService, TransferService transferService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody AccountCreationRequest request) {
        Account account = accountService.createAccount(request.getUserId(), request.getAccountType());
        if (account != null) {
            return new ResponseEntity<>(account, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Account creation failed", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<?> updateAccount(@PathVariable Long accountId, @RequestBody Account accountDetails) {
        Account updatedAccount = accountService.updateAccount(accountId, accountDetails);
        if (updatedAccount != null) {
            return ResponseEntity.ok(updatedAccount);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        if (!accounts.isEmpty()) {
            return new ResponseEntity<>(accounts, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable Long accountId) {
        Account account = accountService.getAccountById(accountId);
        if (account != null) {
            return ResponseEntity.ok(account);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId) {
        boolean isDeleted = accountService.deleteAccount(accountId);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/transfers")
    public ResponseEntity<?> transferFunds(@RequestBody TransferRequest transferRequest) {
        try {
            Transaction transaction = transferService.transferFunds(
                transferRequest.getFromAccount(),
                transferRequest.getToAccount(),
                transferRequest.getAmount(),
                transferRequest.getCategory()  );
            
      //  	System.out.println("Sendin this to front end "+ transaction);
        	
            if (transaction != null) {
            	System.out.println("Sendin this to front end "+ ResponseEntity.ok(transaction));// Assuming transaction is successful and not null
                return ResponseEntity.ok(transaction);
                
            } else {
                // Handling the case where transaction is null, indicating failure
                // Though typically, you'd throw an exception rather than returning null for failures
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Transfer failed due to invalid transaction details.");
            }
        } catch (Exception e) {
            // Exception handling, e.g., if transaction couldn't be processed
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }
    @GetMapping("/balance/{accountId}")
    public ResponseEntity<?> getAccountBalance(@PathVariable Long accountId) {
        try {
            Account account = accountService.getAccountById(accountId);
            if (account != null) {
                return ResponseEntity.ok(account.getBalance());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching account balance: " + e.getMessage());
        }
    }

    @GetMapping("/transactions/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccountId(@PathVariable Long accountId) {
          System.out.println("I m here.....");;
        List<Transaction> transactions = transactionService.getTransactionHistoryByAccountId(accountId);
       
        if (!transactions.isEmpty()) {
        	 System.out.println("Transactions Details by Account ID"+transactions.toString());
            return ResponseEntity.ok(transactions);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
