import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.services.qr_generator import qr_generator


client = TestClient(app)


def test_generate_qr_code_success():
    """Test successful QR code generation"""
    payload = {
        "code": "QR-TEST12345",
        "bar_id": 1,
        "amount": "50.00"
    }
    
    response = client.post("/api/qr/generate", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    
    assert data["success"] is True
    assert data["message"] == "QR code generated successfully"
    assert data["qr_image_data"] is not None
    assert data["qr_image_data"].startswith("data:image/png;base64,")


def test_generate_qr_code_invalid_format():
    """Test QR generation with invalid code format"""
    payload = {
        "code": "INVALID-CODE",
        "bar_id": 1,
        "amount": "50.00"
    }
    
    response = client.post("/api/qr/generate", json=payload)
    
    assert response.status_code == 400
    assert "Invalid QR code format" in response.json()["detail"]


def test_generate_qr_code_missing_fields():
    """Test QR generation with missing required fields"""
    payload = {
        "code": "QR-TEST12345"
        # Missing bar_id and amount
    }
    
    response = client.post("/api/qr/generate", json=payload)
    
    assert response.status_code == 422  # Validation error


def test_qr_generator_validate_format():
    """Test QR code format validation"""
    assert qr_generator.validate_qr_format("QR-ABC123") is True
    assert qr_generator.validate_qr_format("INVALID") is False
    assert qr_generator.validate_qr_format("QR-") is False
    assert qr_generator.validate_qr_format("") is False


def test_qr_generator_generate_qr_code():
    """Test QR code generation service"""
    result = qr_generator.generate_qr_code(
        code="QR-TEST99999",
        bar_id=1,
        amount="100.00"
    )
    
    assert result is not None
    assert isinstance(result, str)
    assert result.startswith("data:image/png;base64,")


def test_qr_generation_different_amounts():
    """Test QR generation with different amounts"""
    amounts = ["10.00", "50.50", "1000.99"]
    
    for amount in amounts:
        payload = {
            "code": f"QR-TEST-{amount.replace('.', '')}",
            "bar_id": 1,
            "amount": amount
        }
        
        response = client.post("/api/qr/generate", json=payload)
        
        assert response.status_code == 200
        assert response.json()["success"] is True


@pytest.mark.asyncio
async def test_qr_generation_concurrent():
    """Test concurrent QR generation"""
    import asyncio
    
    async def generate_qr(code_suffix: int):
        payload = {
            "code": f"QR-CONCURRENT{code_suffix}",
            "bar_id": 1,
            "amount": "50.00"
        }
        response = client.post("/api/qr/generate", json=payload)
        return response.status_code == 200
    
    # Generate 10 QR codes concurrently
    results = await asyncio.gather(*[generate_qr(i) for i in range(10)])
    
    assert all(results)
