package by.lebenkov.messenger.repository;

import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    Optional<Conversation> findByParticipantsIn(List<ConversationParticipant> participants);

    @Modifying
    @Query(value = "UPDATE conversation_participant SET conversation_id = :conversationId WHERE id_participant = :participantId",
            nativeQuery = true)
    void updateParticipantConversation(@Param("participantId") Integer participantId, @Param("conversationId") Integer conversationId);
}
