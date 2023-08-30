package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Account;

import java.util.Optional;

public interface AccountService {
    void save(Account account);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);
}
