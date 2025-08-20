package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

}
