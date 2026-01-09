import pytest
from fastapi.testclient import TestClient
from app.main import app


client = TestClient(app)


def test_health_check():
    """Test health check endpoint"""
    response = client.get("/health")
    
    assert response.status_code == 200
    data = response.json()
    
    assert data["status"] == "healthy"
    assert data["service"] == "QR Service"
    assert "version" in data


def test_readiness_check():
    """Test readiness probe"""
    response = client.get("/health/ready")
    
    assert response.status_code == 200
    data = response.json()
    
    assert data["status"] == "ready"
    assert data["service"] == "QR Service"
    assert "gateway_reachable" in data


def test_liveness_check():
    """Test liveness probe"""
    response = client.get("/health/live")
    
    assert response.status_code == 200
    data = response.json()
    
    assert data["status"] == "alive"
    assert data["service"] == "QR Service"


def test_metrics_endpoint():
    """Test Prometheus metrics endpoint"""
    response = client.get("/metrics")
    
    assert response.status_code == 200
    assert "text/plain" in response.headers["content-type"]
    
    content = response.text
    
    # Check for expected metrics
    assert "qr_codes_generated_total" in content
    assert "qr_validations_total" in content
    assert "http_requests_total" in content


def test_root_endpoint():
    """Test root endpoint"""
    response = client.get("/")
    
    assert response.status_code == 200
    data = response.json()
    
    assert data["service"] == "QR Service"
    assert data["status"] == "running"
    assert "version" in data
    assert "docs" in data
    assert "health" in data


def test_docs_endpoint():
    """Test API documentation endpoints"""
    # Swagger UI
    response = client.get("/docs")
    assert response.status_code == 200
    
    # ReDoc
    response = client.get("/redoc")
    assert response.status_code == 200


def test_openapi_schema():
    """Test OpenAPI schema endpoint"""
    response = client.get("/openapi.json")
    
    assert response.status_code == 200
    schema = response.json()
    
    assert "openapi" in schema
    assert "info" in schema
    assert schema["info"]["title"] == "QR Service"
