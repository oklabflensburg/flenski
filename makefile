.PHONY: build

# Build the frontend and code and copy the assets to the public folder
build-frontend:
	cd frontend; \
	docker compose up -d; \
	while [ "$$(docker compose ps -q app | xargs docker inspect -f '{{.State.Running}}')" != "true" ]; do \
		echo "waiting to start container..."; \
		sleep 1; \
	done; \
	mkdir -p ../backend/src/main/resources/static; \
	docker compose exec app npm run build; \
	docker compose cp app:/app/flenski/dist/. ../backend/src/main/resources/static/

# Build the backend and frontend for production use
build:
	docker compose build backend qdrant postgres node
	make build-frontend

# Build and run the app for production use
run:
	docker compose up backend qdrant postgres node

# Build and run the app for dev use
run-dev:
	docker compose up backend qdrant postgres node