package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountServiceImp implements AccountService {
    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImp(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void save(Account account) {
        accountRepository.save(account);
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }
}
