package com.example.ue_tracker.repository;

import com.example.ue_tracker.model.UeEventHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UeEventHistoryRepository extends JpaRepository<UeEventHistoryEntity, Long> {

    @Query("SELECT h FROM UeEventHistoryEntity h WHERE h.imsiOrSupi = :imsiOrSupi ORDER BY h.updatedAt DESC")
    Page<UeEventHistoryEntity> findHistoryByImsiOrSupi(@Param("imsiOrSupi") String imsiOrSupi, Pageable pageable);
}
