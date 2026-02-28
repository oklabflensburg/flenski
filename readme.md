# Flenski
Flenski is a information retrieval system which provides information about administrative data about the city Flensburg.
It uses a LLM for semantical search and generating answers to user's questions.

### Requirements
- Docker and Docker Compose
- Open AI API key

## Building the app
### Environment variables
- set up .env file, use .env-example as template
- same for .env in frontend/flensk/.env, here the uri to the backend api needs to be updated

### Build the main app
For Production:
- run `make prod` for production build. This builds the frontend and the backend.
- run `make run` to run the app

For Development:
- run `make dev` for development build
- run `make build-frontent`to build the frontend
- run `docker compose up` inside the frontend folder to build the frontend for development