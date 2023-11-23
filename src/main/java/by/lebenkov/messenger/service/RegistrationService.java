package by.lebenkov.messenger.service;

import by.lebenkov.messenger.dto.AccountDTO;
import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.repository.AccountRepository;
import by.lebenkov.messenger.util.UserRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RegistrationService {

    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;
    ModelMapper modelMapper;

    @Transactional
    public void register(Account account) {
        if (account == null) {
            log.error("Attempted to register a null account.");
            return;
        }

        try {
            if (account.getPassword() != null) {
                account.setPassword(passwordEncoder.encode(account.getPassword()));
            }

            account.setRole(UserRole.ROLE_USER.name());
            account.setIsVerifiedEmail(false);

            accountRepository.save(account);

            log.info("Account registered successfully: {}", account.getUsername());
        } catch (Exception e) {
            log.error("Error registering account: {}", e.getMessage(), e);
        }
    }

    public Account convertToAccount(AccountDTO accountDTO) {
        return this.modelMapper.map(accountDTO, Account.class);
    }
}