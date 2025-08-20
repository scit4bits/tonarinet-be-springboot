package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {

}
