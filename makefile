.PHONY: build
build-frontend:
	cd frontend; \
	docker compose up -d; \
	while [ "$$(docker compose ps -q app | xargs docker inspect -f '{{.State.Running}}')" != "true" ]; do \
		echo "waiting to start container..."; \
		sleep 1; \
	done; \
	docker compose exec app npm run build;
	cd ..;
	rm -f backend/src/main/resources/static/*.html;
	rm -fR backend/src/main/resources/static/assets;
	cp -Rf frontend/flenski/dist/* backend/src/main/resources/static;

build:
	docker compose build backend qdrant postgres node

run:
	docker compose up -d backend qdrant postgres node --remove-orphans

run-dev:
	docker compose up qdrant postgres node --remove-orphans

compile:
	cd backend && ./mvnw clean compile && cd ..

scrape:
	docker compose exec node node /app/scraper/ratsversammlung/scrape.js

move:
	mv /opt/flenski/scripts/results/termine/* /opt/flenski/data/scraped/ratsversammlung/