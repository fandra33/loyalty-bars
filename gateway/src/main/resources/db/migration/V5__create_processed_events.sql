-- Create table for storing processed event ids
CREATE TABLE processed_events (
  id SERIAL PRIMARY KEY,
  event_id VARCHAR(255) NOT NULL UNIQUE,
  processed_at TIMESTAMP NOT NULL DEFAULT now()
);

