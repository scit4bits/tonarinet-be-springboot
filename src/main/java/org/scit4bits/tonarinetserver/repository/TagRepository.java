package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Tag.TagId> {

}
