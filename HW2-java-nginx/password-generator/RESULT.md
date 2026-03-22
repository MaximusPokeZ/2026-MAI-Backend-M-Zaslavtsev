# Отчет по нагрузочному тестированию

**Стек:** Java (Spring Boot) + Nginx  
**Инструмент тестирования:** `wrk`  
**CPU:** 12 ядер (`sysctl -n hw.ncpu`)

---

## 1. Измерение на location public (Статика Nginx)

Тестировалась отдача Nginx статического файла (`barca.jpg`) напрямую с диска.

```bash
wrk -t12 -c100 -d10s http://localhost:8080/public/barca.jpg
```

**Результат:**
```text
Running 10s test @ http://localhost:8080/public/barca.jpg
  12 threads and 100 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.18ms  269.79us  15.00ms   95.43%
    Req/Sec     2.5k     84.24     3.49k    92.42%
  301971 requests in 10.02s, 16.94GB read
Requests/sec:  30139.41
Transfer/sec:      1.69GB
```
> **Вывод:** Сервер показывает огромную пропускную способность (более **30 000 RPS**), так как Nginx оптимизирован для отдачи статических файлов.

---

## 2. Измерение напрямую на приложение (порт 8081)

Тестировалось само приложение в обход Nginx. Из-за искусственной задержки в генерации пароля (50 мс) максимальная пропускная способность упирается в пулы потоков приложения и операционной системы.

### Этап А: Поиск стабильного максимума
Найден предел соединений (`-c2500`), при котором сервер справляется с нагрузкой без отказов.

```bash
wrk -t12 -c2500 -d10s http://localhost:8081
```

**Результат:**
```text
Running 10s test @ http://localhost:8081
  12 threads and 2500 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   640.62ms  403.75ms   1.03s    68.86%
    Req/Sec   312.11    109.43     1.10k    75.15%
  37200 requests in 10.08s, 4.46MB read
Requests/sec:   3688.86
Transfer/sec:    453.11KB
```
> **Результат:** 3688 RPS. Потерь и таймаутов нет.

### Этап Б: Увеличение нагрузки до появления ошибок
Количество соединений увеличено сверх предела (`-c 2600`).

```bash
wrk -t12 -c2600 -d10s http://localhost:8081
```

**Результат:**
```text
Running 10s test @ http://localhost:8081
  12 threads and 2600 connections
  ...
  37273 requests in 10.08s, 4.47MB read
  Socket errors: connect 49, read 0, write 0, timeout 0
Requests/sec:   3697.22
```
> **Вывод:** Произошло переполнение очереди операционной системы. Появились ошибки `Socket errors: connect 49` (новые подключения начали отбрасываться).

---

## 3. Измерение проксирования через Nginx (location pswd-gen)

Тестировалась работа связки `Nginx -> Java-приложение (узкое горлышко)`. При проксировании расход сетевых ресурсов (сокетов) увеличивается, поэтому точка отказа наступает при меньшем количестве одновременных пользователей со стороны клиента.

### Этап А: Базовая пиковая нагрузка
При `-c 400` связка работает стабильно.

```bash
wrk -t12 -c400 -d10s http://localhost:8080/pswd-gen
```

**Результат:**
```text
Running 10s test @ http://localhost:8080/pswd-gen
  12 threads and 400 connections
  ...
  37147 requests in 10.05s, 6.09MB read
Requests/sec:   3695.54
Transfer/sec:    619.89KB
```
> **Результат:** 3695 RPS. Ошибок нет. Nginx успешно удерживает соединения и проксирует ответы от приложения.

### Этап Б: Увеличение нагрузки на 10%+
Количество соединений увеличено до `-c 500`.

```bash
wrk -t12 -c500 -d10s http://localhost:8080/pswd-gen
```

**Результат:**
```text
Running 10s test @ http://localhost:8080/pswd-gen
  12 threads and 500 connections
  ...
  37453 requests in 10.08s, 6.14MB read
  Socket errors: connect 0, read 39733, write 0, timeout 0
  Non-2xx or 3xx responses: 51
Requests/sec:   3714.67
```
> **Вывод:** Из-за того, что Java-приложение перестало успевать отвечать, Nginx начал получать таймауты при проксировании (upstream time out). В результате:
> 1. Появились ошибки чтения сокетов со стороны клиента (`read 39733`).
> 2. Nginx начал сам возвращать ошибки `502 Bad Gateway` (зафиксирован 51 ответ со статусом `Non-2xx`).

---