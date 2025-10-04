-- Extensions (enable helpful Postgres features)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;

-- =====================
-- Users
-- =====================
CREATE TABLE IF NOT EXISTS app_user (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email       TEXT UNIQUE NOT NULL,
  handle      TEXT UNIQUE NOT NULL,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- =====================
-- Conversations (1:1 or group)
-- =====================
CREATE TABLE IF NOT EXISTS conversation (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  is_group         BOOLEAN NOT NULL DEFAULT FALSE,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_message_at  TIMESTAMPTZ
);

-- =====================
-- Participants
-- =====================
CREATE TABLE IF NOT EXISTS conversation_participant (
  conversation_id  UUID REFERENCES conversation(id) ON DELETE CASCADE,
  user_id          UUID REFERENCES app_user(id) ON DELETE CASCADE,
  joined_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  is_admin         BOOLEAN NOT NULL DEFAULT FALSE,
  muted_until      TIMESTAMPTZ,
  PRIMARY KEY (conversation_id, user_id)
);

-- =====================
-- Messages
-- =====================
CREATE TABLE IF NOT EXISTS message (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
  sender_id       UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
  body            TEXT NOT NULL,
  media_url       TEXT,
  meta            JSONB,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  edited_at       TIMESTAMPTZ,
  deleted_at      TIMESTAMPTZ
);

-- =====================
-- Read receipts
-- =====================
CREATE TABLE IF NOT EXISTS message_read (
  message_id  UUID REFERENCES message(id) ON DELETE CASCADE,
  user_id     UUID REFERENCES app_user(id) ON DELETE CASCADE,
  read_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (message_id, user_id)
);

-- =====================
-- Reactions
-- =====================
CREATE TABLE IF NOT EXISTS message_reaction (
  message_id  UUID REFERENCES message(id) ON DELETE CASCADE,
  user_id     UUID REFERENCES app_user(id) ON DELETE CASCADE,
  reaction    TEXT NOT NULL,
  reacted_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (message_id, user_id, reaction)
);

-- =====================
-- Blocks
-- =====================
CREATE TABLE IF NOT EXISTS user_block (
  blocker_id  UUID REFERENCES app_user(id) ON DELETE CASCADE,
  blocked_id  UUID REFERENCES app_user(id) ON DELETE CASCADE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (blocker_id, blocked_id)
);

-- =====================
-- Indexes (for speed)
-- =====================
CREATE INDEX IF NOT EXISTS idx_msg_convo_created_desc ON message (conversation_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_msg_sender_created     ON message (sender_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_msg_body_trgm          ON message USING GIN (body gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_msg_meta_gin           ON message USING GIN (meta);
CREATE INDEX IF NOT EXISTS idx_participant_user       ON conversation_participant (user_id);
CREATE INDEX IF NOT EXISTS idx_convo_last_msg         ON conversation (last_message_at DESC);

