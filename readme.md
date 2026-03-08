# Flenski
Flenski is a information retrieval system which provides information about administrative data about the city Flensburg.
It uses a LLM for semantical search and generating answers to user's questions.

### Requirements
- Docker and Docker Compose
- Open AI API key

## Build the frontend
- set uri to backend api in frontend/.env file
- run `make build-frontend`

## Build for development
In dev mode java is running directly on host machine to make integration with IDE and AI Assistants easier.

- install Java JDK 25 on your host machine
- set up .env file 
- load .env into Environment `source .env` and/or configure it to be loaded with IDE's run config
- build the frontend (see above)
- `make run compile`
- `make run-dev`

### Build and run for production
For Production:
- build the frontend (see above)
- set up .env file
- `make build`
- `make run`