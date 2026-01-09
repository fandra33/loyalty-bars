from typing import Tuple
from app.config import settings
from app.utils.logger import logger
from app.utils.metrics import qr_validation_total, qr_validation_duration
import time


class QRValidator:
    """Service for validating QR codes"""
    
    def __init__(self):
        self.secret = settings.qr_service_secret
    
    def validate_qr_code(self, code: str) -> Tuple[bool, str, str]:
        """
        Validate QR code
        
        Args:
            code: QR code string to validate
        
        Returns:
            Tuple of (is_valid, message, reason)
        """
        start_time = time.time()
        
        try:
            logger.info(f"Validating QR code: {code}")
            
            # Basic format validation
            if not code or len(code) < 5:
                duration = time.time() - start_time
                qr_validation_duration.observe(duration)
                qr_validation_total.labels(result='invalid_format').inc()
                return False, "Invalid QR code", "Code is too short"
            
            if not code.startswith("QR-"):
                duration = time.time() - start_time
                qr_validation_duration.observe(duration)
                qr_validation_total.labels(result='invalid_format').inc()
                return False, "Invalid QR code", "Code must start with 'QR-'"
            
            # Additional validation logic
            # In a real system, you might check against a database or cache
            # For now, we just validate the format
            
            # Check for invalid characters
            allowed_chars = set("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-")
            if not all(c in allowed_chars for c in code):
                duration = time.time() - start_time
                qr_validation_duration.observe(duration)
                qr_validation_total.labels(result='invalid_characters').inc()
                return False, "Invalid QR code", "Code contains invalid characters"
            
            # If all checks pass
            duration = time.time() - start_time
            qr_validation_duration.observe(duration)
            qr_validation_total.labels(result='valid').inc()
            
            logger.info(f"QR code validated successfully: {code} in {duration:.3f}s")
            
            return True, "QR code is valid", None
            
        except Exception as e:
            duration = time.time() - start_time
            qr_validation_total.labels(result='error').inc()
            logger.error(f"Error validating QR code {code}: {str(e)}")
            return False, "Validation error", str(e)
    
    def check_qr_security(self, code: str) -> bool:
        """
        Additional security checks for QR code
        
        Args:
            code: QR code to check
        
        Returns:
            True if security checks pass
        """
        # Implement additional security checks here
        # For example: rate limiting, IP checks, etc.
        
        # Check for SQL injection patterns
        dangerous_patterns = ["'", '"', ";", "--", "/*", "*/", "DROP", "DELETE"]
        code_upper = code.upper()
        
        for pattern in dangerous_patterns:
            if pattern in code_upper:
                logger.warning(f"Security check failed for QR code: {code}")
                return False
        
        return True


# Global instance
qr_validator = QRValidator()
