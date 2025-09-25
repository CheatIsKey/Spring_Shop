-- 1) users 테이블 없으면 생성(실 운영에선 이미 있으므로 거의 skip)
CREATE TABLE IF NOT EXISTS public.users (
    id BIGSERIAL PRIMARY KEY
);

-- 2) id_user 컬럼이 없다면 생성
ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS id_user VARCHAR(255);

-- 3) 레거시 login_id가 있을 때만 동작
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'login_id'
    ) THEN
        -- 3-1) login_id 값이 있는데 id_user가 비어있다면 복사
        EXECUTE 'UPDATE public.users SET id_user = login_id
                 WHERE id_user IS NULL AND login_id IS NOT NULL';

        -- 3-2) id_user를 NOT NULL로 고정(로그인 키는 필수)
        BEGIN
            EXECUTE 'ALTER TABLE public.users ALTER COLUMN id_user SET NOT NULL';
        EXCEPTION
            WHEN others THEN
                -- 만약 기존 데이터에 NULL이 남아있으면 위 UPDATE로 채워졌어야 함
                -- 그래도 실패하면 NOT NULL만 생략(이후 수동 점검)
                RAISE NOTICE 'Could not set NOT NULL on id_user. Please review data.';
        END;

        -- 3-3) login_id 관련 제약/인덱스가 남아있어도 컬럼과 함께 정리
        EXECUTE 'ALTER TABLE public.users DROP COLUMN login_id CASCADE';
    END IF;
END $$;

-- 4) id_user 유니크 인덱스 보장(이미 있으면 skip)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE schemaname = 'public' AND indexname = 'uk_users_id_user'
    ) THEN
        EXECUTE 'CREATE UNIQUE INDEX uk_users_id_user ON public.users(id_user)';
    END IF;
END $$;

-- 5) 기타 컬럼들(없으면 생성) - 엔티티와 정합 유지
ALTER TABLE public.users
    ADD COLUMN IF NOT EXISTS name     VARCHAR(255),
    ADD COLUMN IF NOT EXISTS phone    VARCHAR(50),
    ADD COLUMN IF NOT EXISTS password VARCHAR(255),
    ADD COLUMN IF NOT EXISTS role     VARCHAR(50),
    ADD COLUMN IF NOT EXISTS state    VARCHAR(255),
    ADD COLUMN IF NOT EXISTS city     VARCHAR(255),
    ADD COLUMN IF NOT EXISTS street   VARCHAR(255);