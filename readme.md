# Build information

Build the main app:
- run `make prod` for production build. 
- run `make dev` for development build
- run `make build-frontent`to build the frontend
- run `docker compose up` inside the frontend folder to build the frontend for development



| Ports |                      |
|-------|----------------------|
| 8081  | Frontend und API     |
| 5173  | Frontend dev         |
| 5005  | JVM Remote Debugging |
| 6334  | Qdrant API           |
| 6333  | Qdrant Dashboard     |
