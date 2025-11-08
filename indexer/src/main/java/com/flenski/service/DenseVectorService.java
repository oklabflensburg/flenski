package com.flenski.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DenseVectorService {

    @Autowired
    private EmbeddingModel embeddingModel;
    
    public float[] embed(Document document) {

        return embeddingModel.embed(document);
    }
}
