import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.services.qr_validator import qr_validator


client = TestClient(app)


def test_validate_qr_code_success():
    """Test successful QR code validation"""
    payload = {
        "code": "QR-VALID12345"
    }
    
    response = client.post("/api/qr/validate", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    
    assert data["valid"] is True
    assert data["message"] == "QR code is valid"
    assert data["reason"] is None


def test_validate_qr_code_invalid_format():
    """Test validation with invalid format"""
    payload = {
        "code": "INVALID-CODE"
    }
    
    response = client.post("/api/qr/validate", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    
    assert data["valid"] is False
    assert "must start with 'QR-'" in data["reason"]


def test_validate_qr_code_too_short():
    """Test validation with too short code"""
    payload = {
        "code": "QR-"
    }
    
    response = client.post("/api/qr/validate", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    
    assert data["valid"] is False


def test_validate_qr_code_invalid_characters():
    """Test validation with invalid characters"""
    payload = {
        "code": "QR-TEST@#$%"
    }
    
    response = client.post("/api/qr/validate", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    
    assert data["valid"] is False
    assert "invalid characters" in data["reason"]


def test_validate_qr_code_security_check():
    """Test security check for malicious patterns"""
    malicious_codes = [
        "QR-TEST'; DROP TABLE--",
        "QR-TEST/*COMMENT*/",
        'QR-TEST"OR"1"="1'
    ]
    
    for code in malicious_codes:
        payload = {"code": code}
        response = client.post("/api/qr/validate", json=payload)
        
        assert response.status_code == 200
        data = response.json()
        
        # Should fail security check
        assert data["valid"] is False


def test_qr_validator_check_security():
    """Test security check method"""
    # Valid codes
    assert qr_validator.check_qr_security("QR-VALID123") is True
    assert qr_validator.check_qr_security("QR-ABC-DEF-123") is True
    
    # Malicious codes
    assert qr_validator.check_qr_security("QR-'; DROP") is False
    assert qr_validator.check_qr_security('QR-"DELETE') is False
    assert qr_validator.check_qr_security("QR-/*HACK*/") is False


def test_validate_multiple_codes():
    """Test validation of multiple valid codes"""
    codes = [
        "QR-ABC123",
        "QR-DEF456",
        "QR-GHI789",
        "QR-TEST-001"
    ]
    
    for code in codes:
        payload = {"code": code}
        response = client.post("/api/qr/validate", json=payload)
        
        assert response.status_code == 200
        assert response.json()["valid"] is True


def test_validate_qr_empty_code():
    """Test validation with empty code"""
    payload = {
        "code": ""
    }
    
    response = client.post("/api/qr/validate", json=payload)
    
    assert response.status_code == 422  # Validation error


@pytest.mark.asyncio
async def test_validation_performance():
    """Test validation performance with multiple requests"""
    import time
    
    start = time.time()
    
    for i in range(100):
        payload = {"code": f"QR-PERF{i:05d}"}
        response = client.post("/api/qr/validate", json=payload)
        assert response.status_code == 200
    
    duration = time.time() - start
    
    # Should complete 100 validations in less than 5 seconds
    assert duration < 5.0
    print(f"100 validations completed in {duration:.2f}s")
