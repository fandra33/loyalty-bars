-- ============================================
-- V2: Date de Test (Seed Data)
-- ============================================

-- IMPORTANT: Parolele sunt BCrypt hash pentru "password123"
-- Hash generat: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- ============================================
-- 1. USERS - Clienți și Administratori
-- ============================================

-- Clienți (CLIENT role)
INSERT INTO users (email, password, first_name, last_name, role, points_balance, active) VALUES
                                                                                             ('client1@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ion', 'Popescu', 'CLIENT', 150, TRUE),
                                                                                             ('client2@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Maria', 'Ionescu', 'CLIENT', 75, TRUE),
                                                                                             ('client3@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Andrei', 'Constantinescu', 'CLIENT', 200, TRUE),
                                                                                             ('client4@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Elena', 'Dumitrescu', 'CLIENT', 50, TRUE);

-- Administratori de Baruri (BAR_ADMIN role)
INSERT INTO users (email, password, first_name, last_name, role, points_balance, active) VALUES
                                                                                             ('admin.greenpub@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Gheorghe', 'Vasilescu', 'BAR_ADMIN', 0, TRUE),
                                                                                             ('admin.bluelounge@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Ana', 'Popescu', 'BAR_ADMIN', 0, TRUE);

-- ============================================
-- 2. BARS - Baruri Partenere
-- ============================================
INSERT INTO bars (name, description, address, phone, owner_id, active) VALUES
                                                                           ('The Green Pub', 'A cozy Irish pub with craft beers and live music', 'Str. Libertății 15, Timișoara', '+40 256 123 456', 5, TRUE),
                                                                           ('Blue Lounge', 'Modern cocktail bar with elegant atmosphere', 'Bulevardul Revoluției 30, Timișoara', '+40 256 789 012', 6, TRUE);

-- ============================================
-- 3. REWARDS - Recompense
-- ============================================
INSERT INTO rewards (bar_id, name, description, points_cost, active) VALUES
-- The Green Pub rewards
(1, 'Free Coffee', 'Get a free coffee of your choice', 10, TRUE),
(1, '20% Discount', 'Get 20% discount on your next order', 50, TRUE),
(1, 'Free Meal', 'Get a complete meal for free', 100, TRUE),
-- Blue Lounge rewards
(2, 'Free Cocktail', 'Get a free signature cocktail', 15, TRUE),
(2, '25% Discount', 'Get 25% discount on your bill', 60, TRUE),
(2, 'VIP Night', 'VIP table reservation with bottle service', 150, TRUE);

-- ============================================
-- 4. TRANSACTIONS - Date de test
-- ============================================
-- Tranzacții pentru client1
INSERT INTO transactions (client_id, bar_id, amount, points_earned, type, description, created_at) VALUES
                                                                                                       (1, 1, 50.00, 50, 'PURCHASE', 'Purchase at The Green Pub', NOW() - INTERVAL '5 days'),
                                                                                                       (1, 1, 30.00, 30, 'PURCHASE', 'Purchase at The Green Pub', NOW() - INTERVAL '3 days'),
                                                                                                       (1, 2, 70.00, 70, 'PURCHASE', 'Purchase at Blue Lounge', NOW() - INTERVAL '1 day');

-- Tranzacții pentru client2
INSERT INTO transactions (client_id, bar_id, amount, points_earned, type, description, created_at) VALUES
                                                                                                       (2, 1, 25.00, 25, 'PURCHASE', 'Purchase at The Green Pub', NOW() - INTERVAL '4 days'),
                                                                                                       (2, 2, 50.00, 50, 'PURCHASE', 'Purchase at Blue Lounge', NOW() - INTERVAL '2 days');

-- Tranzacții pentru client3
INSERT INTO transactions (client_id, bar_id, amount, points_earned, type, description, created_at) VALUES
                                                                                                       (3, 1, 100.00, 100, 'PURCHASE', 'Purchase at The Green Pub', NOW() - INTERVAL '6 days'),
                                                                                                       (3, 1, 50.00, 50, 'PURCHASE', 'Purchase at The Green Pub', NOW() - INTERVAL '4 days'),
                                                                                                       (3, 2, 50.00, 50, 'PURCHASE', 'Purchase at Blue Lounge', NOW() - INTERVAL '2 days');

-- ============================================
-- 5. QR CODES - Exemple (expirate pentru test)
-- ============================================
INSERT INTO qr_codes (code, bar_id, amount, expires_at, used, created_at) VALUES
                                                                              ('QR-EXPIRED1', 1, 50.00, NOW() - INTERVAL '1 day', FALSE, NOW() - INTERVAL '2 days'),
                                                                              ('QR-EXPIRED2', 2, 75.00, NOW() - INTERVAL '2 hours', FALSE, NOW() - INTERVAL '1 day');

-- ============================================
-- Verificare date
-- ============================================
SELECT 'Users created:' AS info, COUNT(*) AS count FROM users
UNION ALL
SELECT 'Bars created:', COUNT(*) FROM bars
UNION ALL
SELECT 'Rewards created:', COUNT(*) FROM rewards
UNION ALL
SELECT 'Transactions created:', COUNT(*) FROM transactions
UNION ALL
SELECT 'QR Codes created:', COUNT(*) FROM qr_codes;