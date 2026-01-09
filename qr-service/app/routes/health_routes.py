# Locație: qr-service/app/routes/health_routes.py
from fastapi import APIRouter, Response
from prometheus_client import generate_latest, CONTENT_TYPE_LATEST
from app.models import HealthResponse
from app.config import settings
from app.services.gateway_client import gateway_client

router = APIRouter(tags=["Health"])


@router.get(
    "/health",
    response_model=HealthResponse,
    summary="Health Check",
    description="Check if the service is healthy"
)
async def health_check():
    """
    Health check endpoint for monitoring.
    
    Returns service status, name, and version.
    """
    return HealthResponse(
        status="healthy",
        service=settings.app_name,
        version=settings.app_version
    )


@router.get(
    "/health/ready",
    summary="Readiness Check",
    description="Check if service is ready to accept requests"
)
async def readiness_check():
    """
    Kubernetes readiness probe endpoint.
    
    Checks if service can accept traffic.
    """
    # Check if gateway is reachable (optional)
    gateway_healthy = await gateway_client.check_gateway_health()
    
    return {
        "status": "ready",
        "service": settings.app_name,
        "gateway_reachable": gateway_healthy
    }


@router.get(
    "/health/live",
    summary="Liveness Check",
    description="Check if service is alive"
)
async def liveness_check():
    """
    Kubernetes liveness probe endpoint.
    
    Simple check that service is running.
    """
    return {
        "status": "alive",
        "service": settings.app_name
    }


@router.get(
    "/metrics",
    summary="Prometheus Metrics",
    description="Expose Prometheus metrics"
)
async def metrics():
    """
    Prometheus metrics endpoint.
    
    Returns metrics in Prometheus format.
    """
    return Response(
        content=generate_latest(),
        media_type=CONTENT_TYPE_LATEST
    )
