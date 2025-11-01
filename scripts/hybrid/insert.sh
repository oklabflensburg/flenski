#!/bin/bash

COLLECTION="hybrid"

# Dokumente-Array

declare -a documents=(
  "Aged Gouda develops a crystalline texture and nutty flavor profile after 18 months of maturation."
  "Mature Gouda cheese becomes grainy and develops a rich, buttery taste with extended aging."
  "Brie cheese features a soft, creamy interior surrounded by an edible white rind."
  "This French cheese has a flowing, buttery center encased in a bloomy white crust."
  "Fresh mozzarella pairs beautifully with ripe tomatoes and basil leaves."
  "Classic Margherita pizza topped with tomato sauce, mozzarella, and fresh basil."
  "Parmesan requires at least 12 months of cave aging to develop its signature sharp taste."
  "Parmigiano-Reggiano's distinctive piquant flavor comes from extended maturation in controlled environments."
  "Grilled cheese sandwiches are the ultimate American comfort food for cold winter days."
  "Croque Monsieur combines ham and Gruyère in France's answer to the toasted cheese sandwich."
)

points="[]"
for doc in "${documents[@]}"; do
  uuid=$(uuidgen | tr -d '-')
  point=$(jq -n \
    --arg id "$uuid" \
    --arg text "$doc" \
    '{
      id: $id,
      vector: {
        dense: {
          text: $text,
          model: "sentence-transformers/all-MiniLM-L6-v2"
        },
        sparse: {
          text: $text,
          model: "Qdrant/bm25"
        }
      },
      payload: { text: $text }
    }'
  )
  points=$(echo "$points" | jq --argjson point "$point" '. += [$point]')
done

curl -X PUT "http://localhost:6333/collections/$COLLECTION/points?wait=true" \
  -H "Content-Type: application/json" \
  -d "{\"points\": $points}"
