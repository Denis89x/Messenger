package by.lebenkov.messenger.repository;

import by.lebenkov.messenger.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByUsernameOrEmail(String username, String email);

/*    @Query("SELECT a FROM Account a ORDER BY size(a.texts) DESC")
    List<Account> findAllByOrderByTextsSizeDesc();*/

    List<Account> findAllByUsernameNot(String username);
}
