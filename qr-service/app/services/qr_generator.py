import qrcode
import io
import base64
from typing import Optional
from app.config import settings
from app.utils.logger import logger
from app.utils.metrics import qr_generation_total, qr_generation_duration
import time


class QRGenerator:
    """Service for generating QR codes"""
    
    def __init__(self):
        self.qr_size = settings.qr_size
        self.qr_border = settings.qr_border
    
    def generate_qr_code(self, code: str, bar_id: int, amount: str) -> Optional[str]:
        """
        Generate QR code image as base64 string
        
        Args:
            code: Unique QR code string
            bar_id: Bar identifier
            amount: Transaction amount
        
        Returns:
            Base64 encoded QR code image with data URI prefix
        """
        start_time = time.time()
        
        try:
            # Create QR code data structure
            qr_data = {
                "code": code,
                "bar_id": bar_id,
                "amount": amount
            }
            
            # Convert to string format
            qr_string = f"CODE:{code}|BAR:{bar_id}|AMOUNT:{amount}"
            
            logger.info(f"Generating QR code: {code} for bar {bar_id}")
            
            # Create QR code instance
            qr = qrcode.QRCode(
                version=1,  # Controls size (1 is smallest)
                error_correction=qrcode.constants.ERROR_CORRECT_L,
                box_size=self.qr_size,
                border=self.qr_border,
            )
            
            # Add data
            qr.add_data(qr_string)
            qr.make(fit=True)
            
            # Create image
            img = qr.make_image(fill_color="black", back_color="white")
            
            # Convert to base64
            buffer = io.BytesIO()
            img.save(buffer, format='PNG')
            buffer.seek(0)
            
            img_base64 = base64.b64encode(buffer.read()).decode('utf-8')
            img_data_uri = f"data:image/png;base64,{img_base64}"
            
            # Record success metrics
            duration = time.time() - start_time
            qr_generation_duration.observe(duration)
            qr_generation_total.labels(status='success').inc()
            
            logger.info(f"QR code generated successfully: {code} in {duration:.3f}s")
            
            return img_data_uri
            
        except Exception as e:
            duration = time.time() - start_time
            qr_generation_total.labels(status='error').inc()
            logger.error(f"Error generating QR code {code}: {str(e)}")
            return None
    
    def validate_qr_format(self, code: str) -> bool:
        """
        Validate QR code format
        
        Args:
            code: QR code string to validate
        
        Returns:
            True if format is valid
        """
        # Check basic format: should start with "QR-"
        if not code.startswith("QR-"):
            logger.warning(f"Invalid QR code format: {code}")
            return False
        
        # Check length
        if len(code) < 5 or len(code) > 255:
            logger.warning(f"Invalid QR code length: {len(code)}")
            return False
        
        return True


# Global instance
qr_generator = QRGenerator()
