-- V14: Support Center (tickets, replies, attachments)

-- 1) support_ticket
CREATE TABLE IF NOT EXISTS public.support_ticket (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL,
    title        VARCHAR(200) NOT NULL,
    category     VARCHAR(20)  NOT NULL,       -- TicketCategory (문자열)
    content      TEXT         NOT NULL,
    is_private   BOOLEAN      NOT NULL DEFAULT FALSE,
    status       VARCHAR(20)  NOT NULL DEFAULT 'OPEN', -- TicketStatus (문자열)
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- FK: users(id)
ALTER TABLE public.support_ticket
    ADD CONSTRAINT fk_ticket_user
    FOREIGN KEY (user_id) REFERENCES public.users(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT;

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_ticket_user       ON public.support_ticket (user_id);
CREATE INDEX IF NOT EXISTS idx_ticket_status     ON public.support_ticket (status);
CREATE INDEX IF NOT EXISTS idx_ticket_created_at ON public.support_ticket (created_at);

-- 2) support_reply
CREATE TABLE IF NOT EXISTS public.support_reply (
    id             BIGSERIAL PRIMARY KEY,
    ticket_id      BIGINT      NOT NULL,
    author_id      BIGINT      NOT NULL,
    content        TEXT        NOT NULL,
    is_staff_reply BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- FK: ticket, author(user)
ALTER TABLE public.support_reply
    ADD CONSTRAINT fk_reply_ticket
    FOREIGN KEY (ticket_id) REFERENCES public.support_ticket(id)
    ON UPDATE RESTRICT ON DELETE CASCADE;

ALTER TABLE public.support_reply
    ADD CONSTRAINT fk_reply_author
    FOREIGN KEY (author_id) REFERENCES public.users(id)
    ON UPDATE RESTRICT ON DELETE RESTRICT;

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_reply_ticket      ON public.support_reply (ticket_id);
CREATE INDEX IF NOT EXISTS idx_reply_author      ON public.support_reply (author_id);
CREATE INDEX IF NOT EXISTS idx_reply_created_at  ON public.support_reply (created_at);

-- 3) attachment (문의글 첨부파일)
CREATE TABLE IF NOT EXISTS public.attachment (
    id         BIGSERIAL PRIMARY KEY,
    ticket_id  BIGINT       NOT NULL,
    file_name  VARCHAR(255) NOT NULL,
    url        TEXT         NOT NULL,
    mime_type  VARCHAR(100),
    size       BIGINT       NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

ALTER TABLE public.attachment
    ADD CONSTRAINT fk_attachment_ticket
    FOREIGN KEY (ticket_id) REFERENCES public.support_ticket(id)
    ON UPDATE RESTRICT ON DELETE CASCADE;

-- 인덱스
CREATE INDEX IF NOT EXISTS idx_attachment_ticket     ON public.attachment (ticket_id);
CREATE INDEX IF NOT EXISTS idx_attachment_created_at ON public.attachment (created_at);

-- 4) 트리거(선택): updated_at 자동 갱신
--    애플리케이션에서 JPA Auditing을 쓰지만, DB 레벨에서도 보조로 맞춰줍니다.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'set_updated_at') THEN
        CREATE OR REPLACE FUNCTION set_updated_at()
        RETURNS TRIGGER AS $f$
        BEGIN
            NEW.updated_at := now();
            RETURN NEW;
        END;
        $f$ LANGUAGE plpgsql;
    END IF;
END$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_ticket_set_updated_at') THEN
        CREATE TRIGGER trg_ticket_set_updated_at
        BEFORE UPDATE ON public.support_ticket
        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_reply_set_updated_at') THEN
        CREATE TRIGGER trg_reply_set_updated_at
        BEFORE UPDATE ON public.support_reply
        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_attachment_set_updated_at') THEN
        CREATE TRIGGER trg_attachment_set_updated_at
        BEFORE UPDATE ON public.attachment
        FOR EACH ROW EXECUTE FUNCTION set_updated_at();
    END IF;
END$$;
