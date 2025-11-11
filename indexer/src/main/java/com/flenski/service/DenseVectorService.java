package com.flenski.service;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flenski.dto.Vector;

@Service
public class DenseVectorService {

    @Autowired
    private EmbeddingModel embeddingModel;
    
    public Vector embed(Document document) {

        float[] embedding =  embeddingModel.embed(document);
        return new Vector(embedding);
    }
}
