-- users 테이블이 없으면 최소 골격 생성 (id PK)
CREATE TABLE IF NOT EXISTS public.users (
    id BIGSERIAL PRIMARY KEY
);

-- 로그인 아이디(엔티티 @Column(name="id_user"))
ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS id_user VARCHAR(255);

-- 엔티티 필드들
ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS name     VARCHAR(255),
    ADD COLUMN IF NOT EXISTS phone    VARCHAR(50),
    ADD COLUMN IF NOT EXISTS password VARCHAR(255),
    ADD COLUMN IF NOT EXISTS role     VARCHAR(50);

-- 임베디드 Address(state/city/street)
ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS state  VARCHAR(255),
    ADD COLUMN IF NOT EXISTS city   VARCHAR(255),
    ADD COLUMN IF NOT EXISTS street VARCHAR(255);

-- id_user 유니크 인덱스 (이미 있으면 건너뜀)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname = 'public' AND indexname = 'uk_users_id_user'
    ) THEN
        CREATE UNIQUE INDEX uk_users_id_user ON public.users(id_user);
    END IF;
END $$;
