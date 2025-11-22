package com.flenski.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndexingConfig {

    @Value("${flenski.indexing.DocumentBuilderService.chunkSize}")
    private int chunkSize;

    @Value("${flenski.indexing.DocumentBuilderService.maxNumChunks}")
    private int maxNumnChunks;

    public int getChunkSize() {
        return chunkSize;
    }
    
    public int getMaxNumnChunks() {
        return maxNumnChunks;
    }
}
