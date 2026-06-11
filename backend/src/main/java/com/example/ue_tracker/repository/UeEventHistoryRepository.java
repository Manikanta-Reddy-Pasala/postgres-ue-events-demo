package com.example.ue_tracker.repository;

import com.example.ue_tracker.model.UeEventHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UeEventHistoryRepository extends JpaRepository<UeEventHistoryEntity, Long> {
    Page<UeEventHistoryEntity> findByImsiOrSupiOrderByUpdatedAtDesc(String imsiOrSupi, Pageable pageable);
}
