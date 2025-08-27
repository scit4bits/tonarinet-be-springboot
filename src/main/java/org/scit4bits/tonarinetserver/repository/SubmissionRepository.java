package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

}
