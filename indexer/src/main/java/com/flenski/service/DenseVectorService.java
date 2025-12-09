package com.flenski.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

import io.qdrant.client.grpc.Points.DenseVector;

@Service
public class DenseVectorService {

    @Autowired
    private EmbeddingModel embeddingModel;
    
    public DenseVector embed(Document document) {
        // TODO: we can batch embed multiple documents here for better performance
        float[] embedding =  embeddingModel.embed(document);
        List<Float> floatList = new ArrayList<>(embedding.length);
        for (float f : embedding) {
            floatList.add(f);
        }
        DenseVector denseVector = DenseVector.newBuilder().addAllData(floatList).build();
        return denseVector;
    }
}
