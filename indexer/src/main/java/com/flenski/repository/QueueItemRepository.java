package com.flenski.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.flenski.entity.QueueItem;

@Repository
public interface QueueItemRepository extends JpaRepository<QueueItem, Long> {
    boolean existsByIdentifier(String identifier);

    List<QueueItem> findAllByOrderByCreatedAtAsc(Pageable pageable);
}