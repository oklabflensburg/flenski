
curl -X PUT "http://localhost:6333/collections/test/points" \
    -H "Content-Type: application/json" \
    -d '
{
  "points": [
    {
      "id": 1,
      "vector": [0.1, 0.2, 0.3, 0.4],
      "payload": {"land": "forest", "color": "green", "life": true, "humidity": 40}
    },
    {
      "id": 2,
      "vector": [0.2, 0.3, 0.4, 0.5],
      "payload": {"land": "lake", "color": "blue", "life": true, "humidity": 100}
    },
    {
      "id": 3,
      "vector": [0.3, 0.4, 0.5, 0.6],
      "payload": {"land": "steppe", "color": "green", "life": false, "humidity": 25}
    },
    {
      "id": 4,
      "vector": [0.4, 0.5, 0.6, 0.7],
      "payload": {"land": "desert", "color": "red", "life": false, "humidity": 5}
    },
    {
      "id": 5,
      "vector": [0.5, 0.6, 0.7, 0.8],
      "payload": {"land": "marsh", "color": "black", "life": true, "humidity": 90}
    },
    {
      "id": 6,
      "vector": [0.6, 0.7, 0.8, 0.9],
      "payload": {"land": "cavern", "color": "black", "life": false, "humidity": 15}
    }
  ]
} 
'
