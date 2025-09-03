package org.scit4bits.tonarinetserver.repository;

import org.scit4bits.tonarinetserver.entity.TaskGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskGroupRepository extends JpaRepository<TaskGroup, Integer>{
    
}
