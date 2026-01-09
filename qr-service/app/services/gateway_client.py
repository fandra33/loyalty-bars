import httpx
from typing import Optional, Dict, Any
from app.config import settings
from app.utils.logger import logger


class GatewayClient:
    """HTTP client for communicating with the Gateway service"""
    
    def __init__(self):
        self.base_url = settings.gateway_url
        self.timeout = 10.0
    
    async def notify_transaction(self, qr_code: str, transaction_data: Dict[str, Any]) -> bool:
        """
        Notify gateway about a validated transaction
        
        Args:
            qr_code: The validated QR code
            transaction_data: Transaction information
        
        Returns:
            True if notification successful
        """
        try:
            async with httpx.AsyncClient(timeout=self.timeout) as client:
                url = f"{self.base_url}/api/qr/notification"
                
                payload = {
                    "qr_code": qr_code,
                    "transaction_data": transaction_data
                }
                
                logger.info(f"Notifying gateway about QR code: {qr_code}")
                
                response = await client.post(url, json=payload)
                response.raise_for_status()
                
                logger.info(f"Gateway notified successfully for QR: {qr_code}")
                return True
                
        except httpx.HTTPError as e:
            logger.error(f"Failed to notify gateway: {str(e)}")
            return False
        except Exception as e:
            logger.error(f"Unexpected error notifying gateway: {str(e)}")
            return False
    
    async def check_gateway_health(self) -> bool:
        """
        Check if gateway service is healthy
        
        Returns:
            True if gateway is healthy
        """
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                url = f"{self.base_url}/actuator/health"
                response = await client.get(url)
                response.raise_for_status()
                
                data = response.json()
                return data.get("status") == "UP"
                
        except Exception as e:
            logger.warning(f"Gateway health check failed: {str(e)}")
            return False


# Global instance
gateway_client = GatewayClient()
