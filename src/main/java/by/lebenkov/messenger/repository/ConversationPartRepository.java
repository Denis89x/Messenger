package by.lebenkov.messenger.repository;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationPartRepository extends JpaRepository<ConversationParticipant, Integer> {
    Optional<ConversationParticipant> findByAccountUsername(String username);
    Optional<ConversationParticipant> findByAccount(Account account);

    List<ConversationParticipant> findAllByAccount(Account account);

    Optional<ConversationParticipant> findById(Integer id);

    List<ConversationParticipant> findAllByAccountUsername(String username);
}
