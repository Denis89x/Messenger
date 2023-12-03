package by.lebenkov.messenger.repository;

import by.lebenkov.messenger.model.Account;
import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationPartRepository extends JpaRepository<ConversationParticipant, Integer> {
    Optional<ConversationParticipant> findByAccountUsername(String username);
    Optional<ConversationParticipant> findByAccount(Account account);

    List<ConversationParticipant> findAllByAccount(Account account);

    Optional<ConversationParticipant> findById(Integer id);

    @Modifying
    @Query("DELETE FROM ConversationParticipant cp WHERE cp.conversation = :conversation")
    void deleteAllByConversation(Conversation conversation);

    List<ConversationParticipant> findAllByAccountUsername(String username);
}
