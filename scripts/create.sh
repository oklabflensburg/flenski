curl -X PUT "http://localhost:6333/collections/test" \
  -H "Content-Type: application/json" \
  -d '{
     "vectors": {
    "size": 4,
    "distance": "Cosine"
    }
  }'