package com.futurebank.accountService.service;


import com.futurebank.accountService.model.Account;
import com.futurebank.accountService.model.MyTransactionCategory;
import com.futurebank.accountService.model.Transaction;
import com.futurebank.accountService.repository.AccountRepository;
import com.futurebank.accountService.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransferServiceImpl implements TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

   
    public TransferServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }


@Override
@Transactional
public Transaction transferFunds(Long fromAccountId, Long toAccountId, BigDecimal amount, MyTransactionCategory category) {
    validateTransferAmount(amount);

    Account fromAccount = findAccountById(fromAccountId, "From");
    Account toAccount = findAccountById(toAccountId, "To");
    validateSufficientFunds(fromAccount, amount);

    updateAccountBalances(fromAccount, toAccount, amount);
    
    // Assuming createAndSaveTransaction returns a Transaction object that includes the transaction time
    Transaction transaction = createAndSaveTransaction(fromAccountId, toAccountId, amount, category,fromAccount.getBalance());
   
    // Assuming fromAccount.getBalance() after updateAccountBalances gives the latest balance
    
    return transaction;
}



@Override
public Transaction transferFunds(Long fromAccountId, Long toAccountId, Double amount, String category) {
    BigDecimal transferAmount = BigDecimal.valueOf(amount);
    MyTransactionCategory transactionCategory = MyTransactionCategory.valueOf(category.toUpperCase());
    // Directly returning the DTO from the overloaded method
    return transferFunds(fromAccountId, toAccountId, transferAmount, transactionCategory);
}

    private void validateTransferAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
    }

    private Account findAccountById(Long accountId, String accountType) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException(accountType + " account not found"));
    }

    private void validateSufficientFunds(Account fromAccount, BigDecimal amount) {
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds in the from account");
        }
    }

    private void updateAccountBalances(Account fromAccount, Account toAccount, BigDecimal amount) {
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
    }

    private Transaction createAndSaveTransaction(Long fromAccountId, Long toAccountId, BigDecimal amount, MyTransactionCategory category,BigDecimal balance) {
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);
        transaction.setAmount(amount);
        transaction.setCategory(category);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAvlBalance(balance);
        transaction = transactionRepository.save(transaction);
     
      
        return transaction;
    }
}
