# Boardgames API — ДЗ №5

Spring Boot + PostgreSQL + Nginx, всё поднимается через Docker Compose.

## Запуск

```bash
# Сборка JAR
./gradlew bootJar

# Поднять все контейнеры (БД + приложение + nginx)
make up
```

Приложение доступно на `http://localhost:8081` (через nginx) или напрямую на `http://localhost:8080`.

## Остановка

```bash
make down
```

Данные БД сохраняются в Docker volume `postgres_data` и **не теряются** при перезапуске.

---

## API endpoints

### GET /api/games/
Возвращает список всех настольных игр из БД.

```bash
curl http://localhost:8081/api/games/
```

Пример ответа:
```json
[
  {"id": 1, "title": "Catan", "description": "Построй поселение", "category": null},
  {"id": 2, "title": "Манчкин", "description": "Убей монстра", "category": null}
]
```

---

### POST /api/games/create
Создаёт новую игру. Принимает JSON в теле запроса.

```bash
curl -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Catan", "description": "Построй поселение и стань богачом"}'
```

Пример ответа:
```json
{"id": 1, "title": "Catan", "description": "Построй поселение и стань богачом", "category": null}
```

---

### GET /api/search?q=...
Ищет игры по вхождению строки в поля `title` **или** `description` (без учёта регистра).

```bash
curl "http://localhost:8081/api/search?q=монопол"
```

Пример ответа:
```json
[
  {"id": 3, "title": "Монополия", "description": "Скупи всё", "category": null}
]
```

---

## Демонстрация работы

```bash
# 1. Поднять проект
./gradlew bootJar && make up

# 2. Добавить несколько игр
curl -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Catan", "description": "Стратегия: построй поселение"}'

curl -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Манчкин", "description": "Убей монстра, получи сокровище"}'

curl -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Монополия", "description": "Скупи всю недвижимость"}'

# 3. Получить все игры
curl http://localhost:8081/api/games/

# 4. Поиск по слову "стратегия" (находит Catan по description)
curl -G "http://localhost:8081/api/search" --data-urlencode "q=стратегия"

либо в браузере http://localhost:8081/api/search?q=стратегия

# 5. Перезапустить — данные сохранятся
make down && make up
curl http://localhost:8081/api/games/
```