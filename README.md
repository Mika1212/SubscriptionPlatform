# Subscription System

Сервис для работы с подписками, инвойсами и пользовательским кешем.  
Архитектура событийная: данные передаются через RabbitMQ, кеш обновляется через Redis.

---

## Запуск инфраструктуры

### RabbitMQ

```bash
docker run -d --name rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### UI:

```
http://localhost:15672
app_user / app_pass
```

### Redis
```
docker run -d --name redis -p 6379:6379 redis:7
```

### Запуск приложения
```
./gradlew bootRun
```


## Основные API
### Подписки
Активация
```
POST /api/subscriptions/activate
{
  "userId": "UUID",
  "type": "BASIC",
  "activationDate": "YYYY-MM-DD"
}
```
Деактивация
```
POST /api/subscriptions/deactivate
{
  "userId": "UUID",
  "type": "BASIC"
}
```
### Пользовательский кеш

Получить данные пользователя
```
GET /api/users/{userId}?page=0&size=10
page >= 0
size: 1–100
```

Ответ содержит:
```
subscriptions
invoices (пагинация)
totalInvoices
Архитектура
Поток данных
сервис подписок пишет события в outbox
scheduler отправляет события в RabbitMQ
consumer обновляет Redis кеш
Очереди RabbitMQ
subscription.queue — активация подписки
unsubscription.queue — деактивация
invoice.queue — создание инвойса
```

### Кеш (Redis)
Ключ:
```
user:{userId}
```

TTL: 1 час

## Важные моменты
### 1. Деактивация подписки
Удаление идёт строго по subscriptionId, не по type.
### 2. Инвойсы
Уникальность: (user_id, billing_date)
Повторное создание на ту же дату игнорируется.
### 3. Биллинг
Инвойсы генерируются раз в сутки (00:00).
Для локального теста можно временно уменьшать интервал.
### 4. Надёжность
retry есть для Redis save и outbox публикации
### TODO
DLQ для RabbitMQ и retry failure flow
устранение race condition при повторной активации подписки
улучшение стратегии обновления кеша
нормальная синхронизация billing процесса
