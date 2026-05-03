-- ═══════════════════════════════════════════════════════════════
--  SEED DATA — runs on every startup (spring.sql.init.mode=always)
--
--  WHY INSERT OR DO NOTHING?
--  On first startup the users table is empty — insert runs fine.
--  On subsequent startups the rows already exist — without
--  ON CONFLICT DO NOTHING the insert would fail with a duplicate
--  key error and the app would crash.
--
--  PASSWORDS:
--  These are BCrypt hashes. The plain text passwords are:
--    player@arena.com    → password123
--    owner@arena.com     → password123
--  Never store plain text passwords — ever.
-- ═══════════════════════════════════════════════════════════════

INSERT INTO
    users (email, password, name, role)
VALUES (
        'player@arena.com',
        '$2a$10$rhumW61jML3OcZaASmUo8e/MtyzSMChsASa2PZzRazVzbg5c8ykPu',
        'Test Player',
        'PLAYER'
    ) ON CONFLICT (email) DO NOTHING;

INSERT INTO
    users (email, password, name, role)
VALUES (
        'owner@arena.com',
        '$2a$10$rhumW61jML3OcZaASmUo8e/MtyzSMChsASa2PZzRazVzbg5c8ykPu',
        'Arena Owner',
        'ARENA_OWNER'
    ) ON CONFLICT (email) DO NOTHING;