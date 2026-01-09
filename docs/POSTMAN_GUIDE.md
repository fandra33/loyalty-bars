Health
Method: GET
URL: {{base_url}}/actuator/health
Expected: 200, body contains "status": "UP"
Test: pm.response.to.have.status(200);


Auth: Register
Method: POST
URL: {{base_url}}/api/auth/register
Body (JSON):
{
"email":"newclient@example.com",
"password":"password123",
"firstName":"New",
"lastName":"Client",
"role":"CLIENT"
}
Expected: 200, returns JwtResponse with token
Negative: invalid email/short password → expect 400 + validation message


Auth: Login
Method: POST
URL: {{base_url}}/api/auth/login
Body:
{
"email": "{{client_email}}",
"password": "{{client_password}}"
}
Expected: 200, response.token exists → save to env (see script above)


Auth: Me
Method: GET
URL: {{base_url}}/api/auth/me
Auth: Bearer token
Expected: 200, profile email matches token user
Negative: without header → expect 401


Bars
GET all: GET {{base_url}}/api/bars → 200 list
GET by id: GET {{base_url}}/api/bars/1 → 200 detail
Search: GET {{base_url}}/api/bars/search?q=Green → 200 filtered
My bar (BAR_ADMIN only): GET {{base_url}}/api/bars/my-bar
As client → expect 403
As admin (login admin, set token) → expect 200


Rewards
GET all: GET {{base_url}}/api/rewards → 200
GET reward: GET {{base_url}}/api/rewards/{{reward_id}} → 200
GET by bar: GET {{base_url}}/api/rewards/bar/{{bar_id}} → 200
Affordable (CLIENT): GET {{base_url}}/api/rewards/affordable (login as client) → 200 and list matches points balance
Create reward (BAR_ADMIN):
POST {{base_url}}/api/rewards/create
Body: { "barId": {{bar_id}}, "name":"Test Reward", "description":"", "pointsCost": 5 }
Expected: 201
Negative: as client → 403
Redeem reward (CLIENT):
POST {{base_url}}/api/rewards/redeem
Body: { "rewardId": 1 }
Expected: 200 and TransactionDTO; check that client points decreased accordingly
Negative: insufficient points → expect 4xx (check body for message)


QR flows
Generate (CLIENT):
POST {{base_url}}/api/qr/generate
Body: { "barId": {{bar_id}}, "amount": 50.00 }
Expected: 200, returns QRCodeResponse (contains qrCode string and expiresAt)
Validate (BAR_ADMIN):
POST {{base_url}}/api/qr/validate
Body: { "qrCode": "QR-xxx..." } (use generated value or a seeded QR)
Expected: 200, returns TransactionDTO
Negative: expired or already used QR → expect 4xx
Notification (internal):
POST {{base_url}}/api/qr/notification
Body: microservice notification shape (Map<String,Object>), usually accepted without auth → 200
Useful to test QR microservice integration manually.


Transactions
My transactions (CLIENT): GET {{base_url}}/api/transactions/my-transactions → 200 list; verify contains recent purchases/redeems
Bar transactions (BAR_ADMIN): GET {{base_url}}/api/transactions/bar/{{bar_id}} → 200
Recent: GET {{base_url}}/api/transactions/recent → 200


Dashboard
Client dashboard: GET {{base_url}}/api/dashboard/client → 200
Bar dashboard: GET {{base_url}}/api/dashboard/bar → 200 (BAR_ADMIN only)


Debug endpoint (public)
POST {{base_url}}/api/debug/log with {"message":"test"} → 200
Useful for verifying request reachability without authentication.