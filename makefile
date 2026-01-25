.PHONY: build

build-frontend:
	cd frontend; \
	docker compose up -d; \
	while [ "$$(docker compose ps -q app | xargs docker inspect -f '{{.State.Running}}')" != "true" ]; do \
		echo "Warte auf Start von app-Container..."; \
		sleep 1; \
	done; \
	docker compose exec app npm run build; \
	docker compose cp app:/app/flenski/dist/. ../backend/src/main/resources/static/

dev:
	docker compose -f docker-compose.yaml -f docker-compose.dev.yaml up

prod:
	docker compose up
