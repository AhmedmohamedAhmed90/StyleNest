# StyleNest Microservices — Getting Started

This guide takes you from zero to a running, observable stack with Docker Compose. It includes base services (Spring + .NET), message broker, databases, and optional monitoring (Prometheus + Grafana). Commands are shown for PowerShell (Windows). For bash/macOS, replace `$env:VAR` with `export VAR`.

## 1) Prerequisites
- Docker Desktop (Compose v2)
- 6–8 GB RAM available for containers

```powershell
cd C:\Users\ZIAD\Documents\GitHub\StyleNest
```

Build all images (Java + .NET services):
```powershell
docker compose build
```
Start core infra and app services:
```powershell
docker compose up -d
```
Verify containers:
```powershell
docker compose ps
```
Tail key logs (Ctrl+C to stop):
```powershell
docker compose logs -f eureka configserver rabbitmq apigateway orderservice productservice paymentservice
```

Endpoints (once healthy):
- Eureka: http://localhost:8761
- API Gateway: http://localhost:8085
- RabbitMQ UI: http://localhost:15672 (guest/guest)

Notes
- PaymentService is configured for Docker networking and auto‑reconnects to RabbitMQ. No extra env needed.
- Gateway forwards to services discovered via Eureka.

## 4) Optional: Start Monitoring (Prometheus + Grafana)
Start the monitoring overlay attached to the app network:
```powershell
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d prometheus grafana
```
Open UIs:
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)



### 5.0 (Optional) Work with Cart
Cart APIs require only the JWT when called through the gateway (the gateway forwards the user id).

Get (or create) the current user's cart:
```powershell
Invoke-RestMethod -Method Get `
  -Uri http://localhost:8085/api/cart `
  -Headers @{ Authorization = "Bearer $token";Content-Type = "application/json" }
```

Add/update a single item:
```powershell
$item = '{"ProductId":"1","Quantity":2,"Price":200}'
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8085/api/cart/items `
  -Headers @{ Authorization = "Bearer $token"; "Content-Type"="application/json" } `
  -Body $item
```

Add/update multiple items in bulk:
```powershell
$items = '[ {"ProductId":"1","Quantity":2,"Price":200}, {"ProductId":"2","Quantity":1,"Price":99} ]'
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8085/api/cart/items/bulk `
  -Headers @{ Authorization = "Bearer $token"; "Content-Type"="application/json" } `
  -Body $items
```

Update quantity for a product:
```powershell
Invoke-RestMethod -Method Put `
  -Uri http://localhost:8085/api/cart/items/1 `
  -Headers @{ Authorization = "Bearer $token"; "Content-Type"="application/json" } `
  -Body 3
```

Remove an item:
```powershell
Invoke-RestMethod -Method Delete `
  -Uri http://localhost:8085/api/cart/items/1 `
  -Headers @{ Authorization = "Bearer $token" }
```

Clear the cart:
```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8085/api/cart/clear `
  -Headers @{ Authorization = "Bearer $token" }
```

Checkout (publishes a cart checkout event and extends cart TTL):
```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8085/api/cart/checkout `
  -Headers @{ Authorization = "Bearer $token" }
```
Note: In this codebase, orders are created via the OrderService endpoint (below). Cart checkout already publishes an event to RabbitMQ for future automation.


### 5.1 Create a category
```powershell
$body = '{"name":"Shoes","description":"Footwear"}'
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8085/api/categories `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body
```

### 5.2 Create a product (link to the category)
Adjust `categoryId` from the previous response (e.g., 1):
```powershell
$body = '{
  "name":"Runner 200",
  "description":"Light running shoe",
  "price":200,
  "stock":50,
  "categoryId":1
}'
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8085/api/products `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body
```

### 5.3 Obtain a JWT (if needed)
Use AuthService endpoints via Gateway (typical pattern; adapt to your schema):
- Register: `POST http://localhost:8085/auth/register`
- Login: `POST http://localhost:8085/auth/login` → returns `{ token: "..." }`

Set your JWT and user id for the next calls:
```powershell
$token = "<YOUR_JWT>"
$headers = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }
```

### 5.4 Place an order (JWT only via Gateway)
```powershell
$orderItems = '[ {"productId":1, "quantity":1, "price":200 } ]'
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8085/api/orders `
  -Headers $headers `
  -Body $orderItems
```

### 5.5 Pay for the order (confirms it)
Replace `orderId` with the id from the previous step (string acceptable).
```powershell
$payment = '{
  "orderId": "1",
  "amount": 200,
  "cardNumber": "4242424242424242",
  "cvv": "123",
  "cardHolderName": "Test User"
}'
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8085/api/payments `
  -Headers $headers `
  -Body $payment
```

Order should be `CONFIRMED` after payment.


### 5.6 Check orders for the user
```powershell
Invoke-RestMethod -Method Get `
  -Uri http://localhost:8085/api/orders `
  -Headers @{ Authorization = "Bearer $token" }
```

Note: When calling via API Gateway, you do NOT need to send `X-USER-ID`; the gateway extracts the user id from the JWT and forwards it as `X-USER-ID` to downstream services. If you bypass the gateway and call a service directly, include `X-USER-ID` yourself.

## 6) Observability quick checks
Prometheus (http://localhost:9090):
- `orders_payment_events_total`
- `orders_status_total`


Grafana (http://localhost:3000):
- Dashboard: Orders & Payments Overview
- If graphs show zero, wait ~30–60s (15s scrape) or click Refresh. Use Last 15m range.


## 8) Useful URLs
- Eureka: http://localhost:8761
- API Gateway Swagger: http://localhost:8085/swagger-ui.html
- RabbitMQ UI: http://localhost:15672 (guest/guest)
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

## 9) Stop and clean up
Stop services (keep volumes):
```powershell
docker compose down
```
Stop monitoring overlay:
```powershell
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml down
```

---
If anything is unclear or you want a one‑liner script to bring everything up, say the word and I’ll add it.
