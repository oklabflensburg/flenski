package com.flenski.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.flenski.dto.DocumentDto;
import com.flenski.entity.QueueItem;
import com.flenski.repository.QueueItemRepository;
import com.flenski.result.QueueResult;

@Service
public class QueueService {

    private final QueueItemRepository queueItemRepository;

    public QueueService(QueueItemRepository queueItemRepository) {
        this.queueItemRepository = queueItemRepository;
    }

    public QueueResult add(DocumentDto documentDto) {
        return add(List.of(documentDto));
    }

    public QueueResult add(List<DocumentDto> documents) {

        AtomicInteger amountDuplicates = new AtomicInteger(0);
        AtomicInteger amountSaved = new AtomicInteger(0);

        List<DocumentDto> filteredDocuments = documents.stream()
                .filter(documentDto -> {
                    if (queueItemRepository.existsByIdentifier(documentDto.getSourceIdentifier())) {
                        return false;
                    } else {
                        return true;
                    }
                })
                .collect(Collectors.toList());

        List<QueueItem> queueItems = filteredDocuments.stream()
                .map(this::mapDocumentDtoToQueueItem)
                .collect(Collectors.toList());

        for (DocumentDto documentDto : documents) {
            QueueItem queueItem = mapDocumentDtoToQueueItem(documentDto);
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

    public void delete(QueueItem queueItem) {
        queueItemRepository.delete(queueItem);
    }

    private QueueItem mapDocumentDtoToQueueItem(DocumentDto documentDto) {
        QueueItem queueItem = new QueueItem();
        queueItem.setIdentifier(documentDto.createHash());
        queueItem.setDocument(documentDto);
        return queueItem;
    }
}
