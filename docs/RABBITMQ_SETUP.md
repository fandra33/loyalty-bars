Local RabbitMQ setup and quick test

1. Start RabbitMQ (management UI on 15672):

   docker compose -f docker-compose.rabbitmq.yml up -d

2. Start the consumer (qr-service):

   python -m venv .venv
   .\.venv\Scripts\Activate.ps1
   pip install -r qr-service/requirements.txt
   python qr-service/app/services/rabbit_consumer.py

3. Trigger a producer message from the gateway service (example):
   - You can run the gateway and call the endpoint that creates a QR code.
   - Or publish a message manually:

   python - <<'PY'
   import pika, json
   params = pika.URLParameters('amqp://guest:guest@localhost:5672/')
   conn = pika.BlockingConnection(params)
   ch = conn.channel()
   ch.queue_declare(queue='qr.requested', durable=True)
   event = {'eventType':'qr.requested','qrCode':'QR-ABC','barId':1,'amount':100}
   ch.basic_publish(exchange='', routing_key='qr.requested', body=json.dumps(event))
   conn.close()
   PY

Notes:
- The consumer acknowledges messages. If processing fails, message will be requeued.
- For production, secure RabbitMQ, enable TLS, credentials, and place behind VPC.

