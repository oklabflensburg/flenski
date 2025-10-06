package com.flenski.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.flenski.dto.QueueResult;
import com.flenski.dto.Record;
import com.flenski.entity.QueueItem;
import com.flenski.repository.QueueItemRepository;

@Service
public class QueueService {

    private final QueueItemRepository queueItemRepository;

    public QueueService(QueueItemRepository queueItemRepository) {
        this.queueItemRepository = queueItemRepository;
    }

    public QueueResult add(Record record) {
        return add(List.of(record));
    }

    public QueueResult add(List<Record> records) {

        AtomicInteger amountDuplicates = new AtomicInteger(0);
        AtomicInteger amountSaved = new AtomicInteger(0);

        List<Record> filteredRecords = records.stream()
                .filter(record -> {
                    if (queueItemRepository.existsByIdentifier(record.getSourceIdentifier())) {
                        return false;
                    } else {
                        return true;
                    }
                })
                .collect(Collectors.toList());

        List<QueueItem> queueItems = filteredRecords.stream()
                .map(this::mapRecordToQueueItem)
                .collect(Collectors.toList());

        for (Record record : records) {
            QueueItem queueItem = mapRecordToQueueItem(record);
            try {
                queueItemRepository.save(queueItem);
            } catch (DataIntegrityViolationException e) {
                amountDuplicates.incrementAndGet();
                continue;
            }
            amountSaved.incrementAndGet();
        }

        return new QueueResult(amountSaved.get(), amountDuplicates.get());
    }

    public List<QueueItem> getNext(int limit) {
        return queueItemRepository.findAllByOrderByCreatedAtAsc(PageRequest.of(0, limit));
    }

    private QueueItem mapRecordToQueueItem(Record record) {
        QueueItem queueItem = new QueueItem();
        queueItem.setIdentifier(record.createHash());
        queueItem.setRecord(record);
        return queueItem;
    }
}
