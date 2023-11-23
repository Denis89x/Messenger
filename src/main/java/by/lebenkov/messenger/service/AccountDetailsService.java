package by.lebenkov.messenger.service;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.repository.AccountRepository;
import by.lebenkov.messenger.security.AccountDetails;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountDetailsService implements UserDetailsService {

    AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<Account> accountByUsername = accountRepository.findByUsername(s);
        if (accountByUsername.isPresent()) {
            return new AccountDetails(accountByUsername.get());
        }

        Optional<Account> accountByEmail = accountRepository.findByEmail(s);
        if (accountByEmail.isPresent()) {
            return new AccountDetails(accountByEmail.get());
        }

        throw new UsernameNotFoundException("User not found");
    }
}
