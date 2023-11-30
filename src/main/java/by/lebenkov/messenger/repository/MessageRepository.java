package by.lebenkov.messenger.repository;

import by.lebenkov.messenger.model.Conversation;
import by.lebenkov.messenger.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByConversationOrderByDateTimeAsc(Conversation conversation);

    Message findFirstByConversationOrderByDateTimeDesc(Conversation conversation);

    void deleteAllByConversation(Conversation conversation);
}