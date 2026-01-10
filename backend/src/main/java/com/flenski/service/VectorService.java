package com.flenski.service;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import io.qdrant.client.grpc.Points.DenseVector;

@Service
public class VectorService {

    public UUID uuid(DenseVector denseVector) {
        List<Float> values = denseVector.getDataList();
        ByteBuffer buffer = ByteBuffer.allocate(values.size() * Float.BYTES);
        for (Float v : values) {
            buffer.putFloat(v);
        }
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(buffer.array());
            ByteBuffer uuidBytes = ByteBuffer.wrap(hash, 0, 16);
            long mostSigBits = uuidBytes.getLong();
            long leastSigBits = uuidBytes.getLong();
            return new UUID(mostSigBits, leastSigBits);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate UUID from DenseVector", e);
        }
    }

}
