# Boardgames API — ДЗ №5 / №7 / №12

Spring Boot + PostgreSQL + Nginx + Centrifugo (WebSocket), всё поднимается через Docker Compose.

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

## ДЗ №7 — Сериализаторы и Generic Views (аналоги DRF)

### Архитектура (Django → Spring аналоги)

| Django / DRF | Spring Boot аналог |
|---|---|
| `serializers.py` + `ModelSerializer` | `serializer/BoardGameSerializer.java`, `serializer/CategorySerializer.java` |
| `ListAPIView` | `@GetMapping("/games/")` |
| `CreateAPIView` | `@PostMapping("/games/create")` |
| `RetrieveAPIView` | `@GetMapping("/games/{id}")` |
| `UpdateAPIView` | `@PutMapping("/games/{id}")` |
| `DestroyAPIView` | `@DeleteMapping("/games/{id}")` |
| `urls.py` | `@RequestMapping` в контроллере |

---

### GET /api/games/{id} — получение игры по id (RetrieveAPIView)

```bash
curl http://localhost:8081/api/games/1
```

Пример ответа:
```json
{"id": 1, "title": "Catan", "description": "Стратегия: построй поселение", "categoryId": null, "categoryName": null}
```

---

### PUT /api/games/{id} — изменение игры (UpdateAPIView)

```bash
curl -X PUT http://localhost:8081/api/games/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Catan (обновлено)", "description": "Новое описание"}'
```

---

### DELETE /api/games/{id} — удаление игры (DestroyAPIView)

```bash
curl -X DELETE http://localhost:8081/api/games/1
# Ответ: 204 No Content
```

---

### Категории — полный CRUD

#### GET /api/categories/ — список всех категорий

```bash
curl http://localhost:8081/api/categories/
```

#### POST /api/categories/ — создание категории

```bash
curl -X POST http://localhost:8081/api/categories/ \
  -H "Content-Type: application/json" \
  -d '{"name": "Стратегии"}'
```

#### GET /api/categories/{id} — детальное описание

```bash
curl http://localhost:8081/api/categories/1
```

#### PUT /api/categories/{id} — изменение категории

```bash
curl -X PUT http://localhost:8081/api/categories/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Стратегии (обновлено)"}'
```

#### DELETE /api/categories/{id} — удаление категории

```bash
curl -X DELETE http://localhost:8081/api/categories/1
# Ответ: 204 No Content
```

---

### Создание игры с категорией

```bash
# Сначала создать категорию
curl -X POST http://localhost:8081/api/categories/ \
  -H "Content-Type: application/json" \
  -d '{"name": "Стратегии"}'
# Ответ: {"id": 1, "name": "Стратегии"}

# Затем создать игру, указав categoryId
curl -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Catan", "description": "Построй поселение", "categoryId": 1}'
# Ответ: {"id": 1, "title": "Catan", ..., "categoryId": 1, "categoryName": "Стратегии"}
```

---

## Демонстрация работы ДЗ №5

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

# 5. Перезапустить — данные сохранятся
make down && make up
curl http://localhost:8081/api/games/
```

---

## Демонстрация работы ДЗ №7

Полный сценарий: создаём категорию, создаём игры с категорией, демонстрируем все CRUD-операции.

```bash
# 0. Поднять проект
./gradlew bootJar && make up

# ── Категории ──────────────────────────────────────────────

# Создать категорию (POST → CreateAPIView)
curl -s -X POST http://localhost:8081/api/categories/ \
  -H "Content-Type: application/json" \
  -d '{"name": "Стратегии"}'
# {"id": 1, "name": "Стратегии"}

# Создать ещё одну категорию
curl -s -X POST http://localhost:8081/api/categories/ \
  -H "Content-Type: application/json" \
  -d '{"name": "Пати-геймы"}'
# {"id": 2, "name": "Пати-геймы"}

# Получить все категории (GET → ListAPIView)
curl -s http://localhost:8081/api/categories/

# Получить категорию по id (GET → RetrieveAPIView)
curl -s http://localhost:8081/api/categories/1

# Изменить категорию (PUT → UpdateAPIView)
curl -s -X PUT http://localhost:8081/api/categories/2 \
  -H "Content-Type: application/json" \
  -d '{"name": "Семейные игры"}'
# {"id": 2, "name": "Семейные игры"}

# ── Игры с категорией ───────────────────────────────────────

# Создать игры, привязав к категориям через categoryId
curl -s -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Catan", "description": "Строй поселения и дороги", "categoryId": 1}'

curl -s -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Манчкин", "description": "Убей монстра, получи сокровище", "categoryId": 2}'

curl -s -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Монополия", "description": "Скупи всю недвижимость города", "categoryId": 2}'

# Получить все игры (GET → ListAPIView) — видим categoryId и categoryName в ответе
curl -s http://localhost:8081/api/games/

# Получить игру по id (GET → RetrieveAPIView)
curl -s http://localhost:8081/api/games/1

# Изменить игру (PUT → UpdateAPIView)
curl -s -X PUT http://localhost:8081/api/games/1 \
  -H "Content-Type: application/json" \
  -d '{"title": "Catan (обновлено)", "description": "Улучшенное издание стратегии"}'

# Проверить изменение
curl -s http://localhost:8081/api/games/1

# Поиск (ищет по title И description)
curl -s -G http://localhost:8081/api/search --data-urlencode "q=стратег"

# Удалить игру (DELETE → DestroyAPIView) — ответ 204 No Content
curl -s -o /dev/null -w "HTTP статус: %{http_code}\n" -X DELETE http://localhost:8081/api/games/3

# Убедиться что игра удалена — список стал короче
curl -s http://localhost:8081/api/games/

# Обращение к несуществующей игре → 404
curl -s -o /dev/null -w "HTTP статус: %{http_code}\n" http://localhost:8081/api/games/999

# Удалить категорию (DELETE → DestroyAPIView)
curl -s -o /dev/null -w "HTTP статус: %{http_code}\n" -X DELETE http://localhost:8081/api/categories/2
```

---

## ДЗ №12 — Centrifugo + WebSocket

### Архитектура

```
Браузер (index.html)
  │  WebSocket ws://localhost:8000
  ▼
Centrifugo (порт 8000) ◄── HTTP API ── Spring Boot (порт 8080)
                                              │
                                              ▼
                                         PostgreSQL
```

1. Браузер открывает `http://localhost:8081/` — Spring Boot отдаёт `index.html`
2. Страница загружает список игр через `GET /api/games/`
3. Страница подключается к Centrifugo по WebSocket и подписывается на канал `games`
4. При `POST /api/games/create` Spring сохраняет игру в БД и публикует событие в Centrifugo
5. Centrifugo доставляет событие браузеру → карточка появляется **без перезагрузки**

### Запуск

```bash
./gradlew bootJar && make up
```

Centrifugo поднимается автоматически вместе с остальными контейнерами.

### Демонстрация работы ДЗ №12

```bash
# 1. Поднять проект
./gradlew bootJar && make up

# 2. Открыть HTML-страницу в браузере
Или вручную: http://localhost:8081/index.html
# Убедиться что статус "WebSocket подключён" (зелёный)

# 3. Добавить данные для красивой демонстрации
curl -s -X POST http://localhost:8081/api/categories/ \
  -H "Content-Type: application/json" \
  -d '{"name": "Стратегии"}'

# 4. В браузере ОТКРЫТА вкладка — НЕ перезагружать!
#    В консоли (второй терминал) создаём игру:
curl -s -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Catan", "description": "Строй поселения, дороги и города", "categoryId": 1}'

# → Карточка "Catan" появляется на странице без перезагрузки!

curl -s -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Манчкин", "description": "Убей монстра, получи сокровище, подставь друга"}'

# → Карточка "Манчкин" появляется сразу!

curl -s -X POST http://localhost:8081/api/games/create \
  -H "Content-Type: application/json" \
  -d '{"title": "Ticket to Ride", "description": "Прокладывай железнодорожные маршруты", "categoryId": 1}'

# → Карточка "Ticket to Ride" появляется сразу!
```

### Как проверить что Centrifugo работает

```bash
# Статус контейнеров
docker ps

# Логи Centrifugo
docker logs boardgames-centrifugo

# Прямой publish через Centrifugo HTTP API (без Spring Boot)
curl -s -X POST http://localhost:8000/api \
  -H "Authorization: apikey boardgames-api-key" \
  -H "Content-Type: application/json" \
  -d '{"method":"publish","params":{"channel":"games","data":{"id":999,"title":"Тестовое сообщение","description":"Отправлено напрямую через Centrifugo API"}}}'
# → На странице появится карточка напрямую из Centrifugo
```