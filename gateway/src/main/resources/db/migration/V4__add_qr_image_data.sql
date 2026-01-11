-- Add qr_image_data column to qr_codes
ALTER TABLE qr_codes
ADD COLUMN qr_image_data text;

