import os
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings loaded from environment variables"""
    
    # Application
    app_name: str = "QR Service"
    app_version: str = "1.0.0"
    debug: bool = False
    
    # Server
    host: str = "0.0.0.0"
    port: int = 8000
    
    qr_service_secret: str = os.getenv("QR_SERVICE_SECRET", "secretdiscret")
    
    gateway_url: str = os.getenv("GATEWAY_URL", "http://localhost:8080")
    
    qr_expiration_minutes: int = 15
    qr_size: int = 10  # QR code box size
    qr_border: int = 2  # QR code border size
    
    log_level: str = "INFO"
    
    class Config:
        env_file = ".env"
        case_sensitive = False


settings = Settings()
