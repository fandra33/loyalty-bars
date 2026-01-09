from prometheus_client import Counter, Histogram, Gauge
import time


qr_generation_total = Counter(
    'qr_codes_generated_total',
    'Total number of QR codes generated',
    ['status']
)

qr_generation_duration = Histogram(
    'qr_generation_duration_seconds',
    'Time spent generating QR codes',
    buckets=[0.001, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0]
)

qr_validation_total = Counter(
    'qr_validations_total',
    'Total number of QR code validations',
    ['result']
)

qr_validation_duration = Histogram(
    'qr_validation_duration_seconds',
    'Time spent validating QR codes',
    buckets=[0.001, 0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0]
)

active_qr_codes = Gauge(
    'active_qr_codes',
    'Number of currently active QR codes'
)

http_requests_total = Counter(
    'http_requests_total',
    'Total HTTP requests',
    ['method', 'endpoint', 'status']
)

http_request_duration = Histogram(
    'http_request_duration_seconds',
    'HTTP request duration',
    ['method', 'endpoint'],
    buckets=[0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0]
)


class MetricsMiddleware:
    """Middleware to track HTTP metrics"""
    
    def __init__(self, app):
        self.app = app
    
    async def __call__(self, scope, receive, send):
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return
        
        method = scope["method"]
        path = scope["path"]
        
        start_time = time.time()
        
        async def send_wrapper(message):
            if message["type"] == "http.response.start":
                status = message["status"]
                duration = time.time() - start_time
                
                http_requests_total.labels(
                    method=method,
                    endpoint=path,
                    status=status
                ).inc()
                
                http_request_duration.labels(
                    method=method,
                    endpoint=path
                ).observe(duration)
            
            await send(message)
        
        await self.app(scope, receive, send_wrapper)
