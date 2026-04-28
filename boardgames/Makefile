
DOCKER_COMPOSE = docker compose

up:
	$(DOCKER_COMPOSE) up -d --build

down:
	$(DOCKER_COMPOSE) down

logs:
	$(DOCKER_COMPOSE) logs -f app

migrate:
	$(DOCKER_COMPOSE) up -d --build
	@echo "Схема БД (миграции) успешно применена через Hibernate!"

reset:
	$(DOCKER_COMPOSE) down -v

