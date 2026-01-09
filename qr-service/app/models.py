from pydantic import BaseModel, Field
from typing import Optional
from decimal import Decimal


class QRGenerationRequest(BaseModel):
    """Request model for QR code generation"""
    code: str = Field(..., min_length=1, max_length=255, description="Unique QR code string")
    bar_id: int = Field(..., gt=0, description="Bar ID")
    amount: str = Field(..., description="Transaction amount")
    
    class Config:
        json_schema_extra = {
            "example": {
                "code": "QR-ABC12345",
                "bar_id": 1,
                "amount": "50.00"
            }
        }


class QRGenerationResponse(BaseModel):
    """Response model for QR code generation"""
    success: bool = Field(..., description="Operation success status")
    message: str = Field(..., description="Response message")
    qr_image_data: Optional[str] = Field(None, description="Base64 encoded QR image")
    
    class Config:
        json_schema_extra = {
            "example": {
                "success": True,
                "message": "QR code generated successfully",
                "qr_image_data": "data:image/png;base64,iVBORw0KGgo..."
            }
        }


class QRValidationRequest(BaseModel):
    """Request model for QR code validation"""
    code: str = Field(..., min_length=1, max_length=255, description="QR code to validate")
    
    class Config:
        json_schema_extra = {
            "example": {
                "code": "QR-ABC12345"
            }
        }


class QRValidationResponse(BaseModel):
    """Response model for QR code validation"""
    valid: bool = Field(..., description="Validation result")
    message: str = Field(..., description="Validation message")
    reason: Optional[str] = Field(None, description="Reason if invalid")
    
    class Config:
        json_schema_extra = {
            "example": {
                "valid": True,
                "message": "QR code is valid",
                "reason": None
            }
        }


class HealthResponse(BaseModel):
    """Health check response"""
    status: str = Field(..., description="Service status")
    service: str = Field(..., description="Service name")
    version: str = Field(..., description="Service version")
    
    class Config:
        json_schema_extra = {
            "example": {
                "status": "healthy",
                "service": "QR Service",
                "version": "1.0.0"
            }
        }


class ErrorResponse(BaseModel):
    """Error response model"""
    error: str = Field(..., description="Error type")
    message: str = Field(..., description="Error message")
    detail: Optional[str] = Field(None, description="Additional error details")
    
    class Config:
        json_schema_extra = {
            "example": {
                "error": "ValidationError",
                "message": "Invalid QR code format",
                "detail": "Code must start with 'QR-'"
            }
        }
