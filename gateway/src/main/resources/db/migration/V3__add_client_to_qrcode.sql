ALTER TABLE qr_codes ADD COLUMN client_id BIGINT;

ALTER TABLE qr_codes
    ADD CONSTRAINT fk_qr_codes_client
    FOREIGN KEY (client_id)
    REFERENCES users (id);

CREATE INDEX idx_qr_codes_client ON qr_codes(client_id);
