curl -X POST "http://localhost:6333/collections/test/points/search" \
 -H "Content-Type: application/json" \
 -d '
{
  "vector": [0.2, 0.1, 0.9, 0.7],
  "using": "dense_vector",
  "limit": 3,
  "with_payload": true
} 
 '