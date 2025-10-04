package com.flenski.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flenski.entity.QueueItem;

@Repository
public interface QueueItemRepository extends JpaRepository<QueueItem, Long> {
    
    boolean existsByIdentifier(String identifier);
    
}