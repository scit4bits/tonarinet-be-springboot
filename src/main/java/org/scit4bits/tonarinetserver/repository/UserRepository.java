package org.scit4bits.tonarinetserver.repository;

import java.util.Optional;

import org.scit4bits.tonarinetserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByOauthidAndProvider(String oauthid, String provider);
}
