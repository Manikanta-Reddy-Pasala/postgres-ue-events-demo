package com.example.ue_tracker.service;

import com.example.ue.proto.UeEvent;
import com.example.ue.proto.UeEventPageResponse;
import com.example.ue_tracker.adapter.UeEventAdapter;
import com.example.ue_tracker.model.UeEventEntity;
import com.example.ue_tracker.model.UeEventHistoryEntity;
import com.example.ue_tracker.repository.UeEventHistoryRepository;
import com.example.ue_tracker.repository.UeEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UeEventService {

    private final UeEventRepository eventRepository;
    private final UeEventHistoryRepository historyRepository;
    private final UeEventAdapter adapter;

    @Transactional
    public void saveEvent(UeEvent proto) {
        UeEventEntity entity = adapter.toEntity(proto);
        eventRepository.save(entity);

        UeEventHistoryEntity historyEntity = adapter.toHistoryEntity(proto);
        historyRepository.save(historyEntity);
    }

    public UeEventPageResponse getLatestEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UeEventEntity> entityPage = eventRepository.findAllByOrderByUpdatedAtDesc(pageable);

        List<UeEvent> protoEvents = entityPage.getContent().stream()
                .map(adapter::toProto)
                .collect(Collectors.toList());

        return UeEventPageResponse.newBuilder()
                .addAllEvents(protoEvents)
                .setTotalPages(entityPage.getTotalPages())
                .setTotalElements(entityPage.getTotalElements())
                .setCurrentPage(entityPage.getNumber())
                .build();
    }

    public UeEventPageResponse getHistory(String imsiOrSupi, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UeEventHistoryEntity> entityPage = historyRepository.findByImsiOrSupiOrderByUpdatedAtDesc(imsiOrSupi, pageable);

        List<UeEvent> protoEvents = entityPage.getContent().stream()
                .map(adapter::toProto)
                .collect(Collectors.toList());

        return UeEventPageResponse.newBuilder()
                .addAllEvents(protoEvents)
                .setTotalPages(entityPage.getTotalPages())
                .setTotalElements(entityPage.getTotalElements())
                .setCurrentPage(entityPage.getNumber())
                .build();
    }
}
