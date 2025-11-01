curl -X PUT http://localhost:6333/collections/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "vectors": {
      "dense": {
        "size": 384,
        "distance": "Cosine"
      }
    },
    "sparse_vectors": {
      "sparse": {
        "modifier": "idf"
      }
    }
}'