package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Integer> {

}
