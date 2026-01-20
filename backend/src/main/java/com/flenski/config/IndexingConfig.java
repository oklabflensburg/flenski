package com.flenski.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexingConfig {

    @Value("${flenski.indexing.DocumentBuilderService.chunkSize}")
    private int chunkSize;

    @Value("${flenski.indexing.DocumentBuilderService.maxNumChunks}")
    private int maxNumnChunks;

    @Value("${flenski.indexing.QueueService.queueBatchSize}")
    private int queueBatchSize = 10;

    @Value("${flenski.indexing.documentSourceDirectoryName}")
    private String documentSourceDirectoryName = "data/scraped/ratsversammlung";

    @Value("${flenski.indexing.docuementQueuedDirectoryName}")
    private String documentQueuedDirectoryName = "data/scraped/ratsversammlung/queued";

    public int getChunkSize() {
        return chunkSize;
    }
    
    public int getMaxNumnChunks() {
        return maxNumnChunks;
    }

    public int getQueueBatchSize() {
        return queueBatchSize;
    }

    public String getDocumentSourceDirectoryName() {
        return documentSourceDirectoryName;
    }
    public String getDocumentQueuedDirectoryName() {
        return documentQueuedDirectoryName;
    }
}
