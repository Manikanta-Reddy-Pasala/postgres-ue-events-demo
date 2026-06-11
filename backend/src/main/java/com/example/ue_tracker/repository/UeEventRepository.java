package com.example.ue_tracker.repository;

import com.example.ue_tracker.model.UeEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UeEventRepository extends JpaRepository<UeEventEntity, String> {
    Page<UeEventEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
