INSERT INTO banks (id, name) VALUES (1, 'T-Bank')
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, username, email, password_hash, role, is_system)
VALUES (1, 'alice', 'alice@mail.com', 'hash1', 'USER', false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, username, email, password_hash, role, is_system)
VALUES (2, 'bob', 'bob@mail.com', 'hash2', 'USER', false)
ON CONFLICT (id) DO NOTHING;

INSERT INTO accounts (id, balance, currency, number, version, user_id, bank_id)
VALUES (1, 5000.00, 'RUBLE', '11112222', 0, 1, 1)
ON CONFLICT (id) DO NOTHING;

INSERT INTO accounts (id, balance, currency, number, version, user_id, bank_id)
VALUES (2, 1000.00, 'RUBLE', '33334444', 0, 2, 1)
ON CONFLICT (id) DO NOTHING;