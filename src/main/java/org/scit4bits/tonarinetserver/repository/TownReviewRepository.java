package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.TownReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TownReviewRepository extends JpaRepository<TownReview, Integer> {

}
