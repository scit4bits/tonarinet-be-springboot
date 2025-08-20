package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {

}
