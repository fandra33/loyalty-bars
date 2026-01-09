from fastapi import APIRouter, HTTPException, status
from app.models import (
    QRGenerationRequest,
    QRGenerationResponse,
    QRValidationRequest,
    QRValidationResponse,
    ErrorResponse
)
from app.services.qr_generator import qr_generator
from app.services.qr_validator import qr_validator
from app.services.gateway_client import gateway_client
from app.utils.logger import logger

router = APIRouter(prefix="/api/qr", tags=["QR Codes"])


@router.post(
    "/generate",
    response_model=QRGenerationResponse,
    status_code=status.HTTP_200_OK,
    summary="Generate QR Code",
    description="Generate a QR code image for a transaction"
)
async def generate_qr_code(request: QRGenerationRequest):
    """
    Generate a QR code for a transaction.
    
    - **code**: Unique QR code string
    - **bar_id**: ID of the bar
    - **amount**: Transaction amount
    
    Returns base64 encoded QR code image.
    """
    try:
        logger.info(f"Received QR generation request: {request.code}")
        
        # Validate QR code format
        if not qr_generator.validate_qr_format(request.code):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid QR code format. Must start with 'QR-'"
            )
        
        # Generate QR code
        qr_image = qr_generator.generate_qr_code(
            code=request.code,
            bar_id=request.bar_id,
            amount=request.amount
        )
        
        if not qr_image:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail="Failed to generate QR code"
            )
        
        return QRGenerationResponse(
            success=True,
            message="QR code generated successfully",
            qr_image_data=qr_image
        )
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error in generate_qr_code: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )


@router.post(
    "/validate",
    response_model=QRValidationResponse,
    status_code=status.HTTP_200_OK,
    summary="Validate QR Code",
    description="Validate a QR code for transaction processing"
)
async def validate_qr_code(request: QRValidationRequest):
    """
    Validate a QR code.
    
    - **code**: QR code string to validate
    
    Returns validation result with reason if invalid.
    """
    try:
        logger.info(f"Received QR validation request: {request.code}")
        
        # Security check
        if not qr_validator.check_qr_security(request.code):
            return QRValidationResponse(
                valid=False,
                message="QR code failed security check",
                reason="Potentially malicious code detected"
            )
        
        # Validate QR code
        is_valid, message, reason = qr_validator.validate_qr_code(request.code)
        
        # Notify gateway (Requirement: communication HTTP with Gateway for transaction confirmation)
        if is_valid:
            await gateway_client.notify_transaction(request.code, {"status": "validated"})
        
        return QRValidationResponse(
            valid=is_valid,
            message=message,
            reason=reason
        )
        
    except Exception as e:
        logger.error(f"Error in validate_qr_code: {str(e)}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Internal server error: {str(e)}"
        )
