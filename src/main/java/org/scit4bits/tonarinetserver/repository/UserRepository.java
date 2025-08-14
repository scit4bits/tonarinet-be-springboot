package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

}
