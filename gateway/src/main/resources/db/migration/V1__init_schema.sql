-- ============================================
-- V1: Schema Inițial pentru Loyalty Bars System
-- ============================================

-- Creează extension pentru UUID (opțional)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- Tabela USERS (Utilizatori)
-- Stochează atât clienți cât și administratori de baruri
-- ============================================
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,  -- Va fi BCrypt hash
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       role VARCHAR(20) NOT NULL CHECK (role IN ('CLIENT', 'BAR_ADMIN', 'SUPER_ADMIN')),
                       points_balance INTEGER DEFAULT 0 CHECK (points_balance >= 0),
                       active BOOLEAN DEFAULT TRUE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pentru căutare rapidă după email
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- ============================================
-- Tabela BARS (Baruri Partenere)
-- ============================================
CREATE TABLE bars (
                      id BIGSERIAL PRIMARY KEY,
                      name VARCHAR(255) NOT NULL,
                      description TEXT,
                      address VARCHAR(500),
                      phone VARCHAR(20),
                      owner_id BIGINT NOT NULL,
                      active BOOLEAN DEFAULT TRUE,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key către users (owner trebuie să fie BAR_ADMIN)
                      CONSTRAINT fk_bars_owner FOREIGN KEY (owner_id)
                          REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_bars_owner ON bars(owner_id);
CREATE INDEX idx_bars_active ON bars(active);

-- ============================================
-- Tabela REWARDS (Recompense)
-- Fiecare bar își definește propriile recompense
-- ============================================
CREATE TABLE rewards (
                         id BIGSERIAL PRIMARY KEY,
                         bar_id BIGINT NOT NULL,
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         points_cost INTEGER NOT NULL CHECK (points_cost > 0),
                         active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_rewards_bar FOREIGN KEY (bar_id)
                             REFERENCES bars(id) ON DELETE CASCADE
);

CREATE INDEX idx_rewards_bar ON rewards(bar_id);
CREATE INDEX idx_rewards_active ON rewards(active);

-- ============================================
-- Tabela TRANSACTIONS (Tranzacții)
-- Înregistrează atât achizițiile cât și răscumpărările
-- ============================================
CREATE TABLE transactions (
                              id BIGSERIAL PRIMARY KEY,
                              client_id BIGINT NOT NULL,
                              bar_id BIGINT NOT NULL,
                              amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
                              points_earned INTEGER NOT NULL,  -- Pozitiv pentru câștig, negativ pentru cheltuire
                              type VARCHAR(20) NOT NULL CHECK (type IN ('PURCHASE', 'REDEMPTION')),
                              description TEXT,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_transactions_client FOREIGN KEY (client_id)
                                  REFERENCES users(id) ON DELETE CASCADE,
                              CONSTRAINT fk_transactions_bar FOREIGN KEY (bar_id)
                                  REFERENCES bars(id) ON DELETE CASCADE
);

CREATE INDEX idx_transactions_client ON transactions(client_id);
CREATE INDEX idx_transactions_bar ON transactions(bar_id);
CREATE INDEX idx_transactions_created ON transactions(created_at DESC);
CREATE INDEX idx_transactions_type ON transactions(type);

-- ============================================
-- Tabela QR_CODES (Coduri QR pentru Validare)
-- Fiecare cod QR este unic și are o durată de valabilitate
-- ============================================
CREATE TABLE qr_codes (
                          id BIGSERIAL PRIMARY KEY,
                          code VARCHAR(255) NOT NULL UNIQUE,
                          bar_id BIGINT NOT NULL,
                          amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
                          expires_at TIMESTAMP NOT NULL,
                          used BOOLEAN DEFAULT FALSE,
                          used_at TIMESTAMP,
                          used_by_id BIGINT,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_qr_codes_bar FOREIGN KEY (bar_id)
                              REFERENCES bars(id) ON DELETE CASCADE,
                          CONSTRAINT fk_qr_codes_used_by FOREIGN KEY (used_by_id)
                              REFERENCES users(id) ON DELETE SET NULL,

    -- Un cod poate fi folosit doar o dată
                          CONSTRAINT chk_qr_used CHECK (
                              (used = FALSE AND used_at IS NULL AND used_by_id IS NULL) OR
                              (used = TRUE AND used_at IS NOT NULL AND used_by_id IS NOT NULL)
                              )
);

CREATE INDEX idx_qr_codes_code ON qr_codes(code);
CREATE INDEX idx_qr_codes_bar ON qr_codes(bar_id);
CREATE INDEX idx_qr_codes_used ON qr_codes(used);
CREATE INDEX idx_qr_codes_expires ON qr_codes(expires_at);

-- ============================================
-- Tabela REDEMPTIONS (Răscumpărări de Recompense)
-- Leagă tranzacțiile de tip REDEMPTION cu reward-urile specifice
-- ============================================
CREATE TABLE redemptions (
                             id BIGSERIAL PRIMARY KEY,
                             transaction_id BIGINT NOT NULL UNIQUE,
                             reward_id BIGINT NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_redemptions_transaction FOREIGN KEY (transaction_id)
                                 REFERENCES transactions(id) ON DELETE CASCADE,
                             CONSTRAINT fk_redemptions_reward FOREIGN KEY (reward_id)
                                 REFERENCES rewards(id) ON DELETE RESTRICT
);

CREATE INDEX idx_redemptions_transaction ON redemptions(transaction_id);
CREATE INDEX idx_redemptions_reward ON redemptions(reward_id);

-- ============================================
-- TRIGGER pentru updated_at
-- Actualizează automat câmpul updated_at când se modifică un record
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bars_updated_at BEFORE UPDATE ON bars
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_rewards_updated_at BEFORE UPDATE ON rewards
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- VIEW-uri utile pentru raportare
-- ============================================

-- View pentru istoricul complet al clienților
CREATE VIEW client_transaction_history AS
SELECT
    t.id AS transaction_id,
    t.created_at,
    u.email AS client_email,
    u.first_name || ' ' || u.last_name AS client_name,
    b.name AS bar_name,
    t.type,
    t.amount,
    t.points_earned,
    t.description
FROM transactions t
         JOIN users u ON t.client_id = u.id
         JOIN bars b ON t.bar_id = b.id
ORDER BY t.created_at DESC;

-- View pentru statistici baruri
CREATE VIEW bar_statistics AS
SELECT
    b.id AS bar_id,
    b.name AS bar_name,
    COUNT(DISTINCT t.client_id) AS unique_clients,
    COUNT(t.id) AS total_transactions,
    SUM(CASE WHEN t.type = 'PURCHASE' THEN t.amount ELSE 0 END) AS total_revenue,
    SUM(CASE WHEN t.type = 'PURCHASE' THEN t.points_earned ELSE 0 END) AS points_given,
    SUM(CASE WHEN t.type = 'REDEMPTION' THEN ABS(t.points_earned) ELSE 0 END) AS points_redeemed
FROM bars b
         LEFT JOIN transactions t ON b.id = t.bar_id
GROUP BY b.id, b.name;

-- ============================================
-- Comments pentru documentație
-- ============================================
COMMENT ON TABLE users IS 'Stochează toți utilizatorii: clienți și administratori de baruri';
COMMENT ON TABLE bars IS 'Barurile partenere din sistem';
COMMENT ON TABLE rewards IS 'Recompensele definite de fiecare bar';
COMMENT ON TABLE transactions IS 'Istoricul tuturor tranzacțiilor (achiziții și răscumpărări)';
COMMENT ON TABLE qr_codes IS 'Coduri QR unice pentru validarea tranzacțiilor';
COMMENT ON TABLE redemptions IS 'Leagă răscumpărările de reward-urile specifice';
