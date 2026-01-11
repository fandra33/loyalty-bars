# Locație: qr-service/app/main.py
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.routes import qr_routes, health_routes
from app.utils.logger import logger
from app.utils.metrics import MetricsMiddleware
from app.services.rabbit_consumer import RabbitConsumer
import threading

# Create FastAPI app
app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description="Microservice for QR code generation and validation in Loyalty Bars System",
    docs_url="/docs",
    redoc_url="/redoc"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Metrics middleware
app.add_middleware(MetricsMiddleware)

# Include routers
app.include_router(qr_routes.router)
app.include_router(health_routes.router)

consumer = None
consumer_thread = None


@app.on_event("startup")
async def startup_event():
    """Actions to perform on application startup"""
    logger.info(f"Starting {settings.app_name} v{settings.app_version}")
    logger.info(f"Debug mode: {settings.debug}")
    logger.info(f"Gateway URL: {settings.gateway_url}")

    # start rabbit consumer in background
    try:
        global consumer, consumer_thread
        consumer = RabbitConsumer()
        consumer_thread = threading.Thread(target=consumer.start_consuming, daemon=True)
        consumer_thread.start()
        logger.info("RabbitMQ consumer started in background thread")
    except Exception as e:
        logger.error(f"Failed to start RabbitMQ consumer: {e}")


@app.on_event("shutdown")
async def shutdown_event():
    """Actions to perform on application shutdown"""
    logger.info(f"Shutting down {settings.app_name}")
    # stop consumer
    try:
        global consumer
        if consumer:
            consumer.close()
    except Exception as e:
        logger.warn(f"Error stopping Rabbit consumer: {e}")


@app.get("/", tags=["Root"])
async def root():
    """Root endpoint with service information"""
    return {
        "service": settings.app_name,
        "version": settings.app_version,
        "status": "running",
        "docs": "/docs",
        "health": "/health"
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=settings.debug,
        log_level=settings.log_level.lower()
    )
