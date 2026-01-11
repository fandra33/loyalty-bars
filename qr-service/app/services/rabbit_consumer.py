import json
import threading
import time
import os
import pika

# Read RabbitMQ connection URL from environment, default to localhost
RABBIT_URL = os.environ.get('RABBITMQ_URL', 'amqp://guest:guest@localhost:5672/')
QUEUE = 'qr.requested'
CREATED_QUEUE = 'qr.created'


def process_message(body):
    try:
        event = json.loads(body)
        print(f"[consumer] Received event: {event}")
        # simulate generating QR image (base64 or URL)
        time.sleep(0.5)
        qr_code = event.get('qrCode')
        # fake image data for demo
        image_data = f"IMAGE_DATA_FOR_{qr_code}"

        # publish qr.created event
        params = pika.URLParameters(RABBIT_URL)
        conn = pika.BlockingConnection(params)
        ch = conn.channel()
        ch.queue_declare(queue=CREATED_QUEUE, durable=True)
        created_event = {
            'eventType': 'qr.created',
            'qrCode': qr_code,
            'qrImageData': image_data,
            'eventId': event.get('eventId', None)
        }
        ch.basic_publish(exchange='', routing_key=CREATED_QUEUE, body=json.dumps(created_event))
        conn.close()

        print(f"[consumer] Published qr.created for {qr_code}")

    except Exception as e:
        print(f"Error processing message: {e}")


class RabbitConsumer:
    def __init__(self, max_retries=5, retry_delay=2):
        self.params = pika.URLParameters(RABBIT_URL)
        self.connection = None
        self.channel = None
        attempts = 0
        while attempts < max_retries:
            try:
                self.connection = pika.BlockingConnection(self.params)
                self.channel = self.connection.channel()
                self.channel.queue_declare(queue=QUEUE, durable=True)
                break
            except Exception as e:
                attempts += 1
                print(f"[consumer] RabbitMQ connection attempt {attempts} failed: {e}")
                time.sleep(retry_delay)
        if self.connection is None:
            raise RuntimeError("Failed to connect to RabbitMQ after retries")

    def start_consuming(self):
        def callback(ch, method, properties, body):
            process_message(body)
            ch.basic_ack(delivery_tag=method.delivery_tag)

        self.channel.basic_qos(prefetch_count=1)
        self.channel.basic_consume(queue=QUEUE, on_message_callback=callback)
        print("[consumer] Starting to consume from RabbitMQ")
        try:
            self.channel.start_consuming()
        except Exception as e:
            print(f"[consumer] Stopped consuming due to: {e}")

    def close(self):
        try:
            if self.channel and not self.channel.is_closed:
                try:
                    self.channel.stop_consuming()
                except Exception:
                    pass
            if self.connection and not self.connection.is_closed:
                self.connection.close()
            print("[consumer] Connection closed")
        except Exception as e:
            print(f"[consumer] Error closing connection: {e}")


if __name__ == '__main__':
    consumer = RabbitConsumer()
    consumer.start_consuming()
