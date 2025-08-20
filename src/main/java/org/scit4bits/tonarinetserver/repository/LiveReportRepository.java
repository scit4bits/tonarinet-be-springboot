package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.LiveReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveReportRepository extends JpaRepository<LiveReport, Integer> {

}
