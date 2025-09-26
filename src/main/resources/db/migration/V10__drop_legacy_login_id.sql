-- V10: 더 이상 쓰지 않는 legacy 칼럼 login_id 제거

-- 1) 혹시 모를 NULL 채움: id_user가 비어 있고 login_id가 있으면 복사
UPDATE public.users
SET id_user = login_id
WHERE id_user IS NULL AND login_id IS NOT NULL;

-- 2) login_id에 걸린 제약/인덱스 제거(있어도 없으면 무시)
ALTER TABLE public.users DROP CONSTRAINT IF EXISTS users_login_id_key;
DROP INDEX IF EXISTS idx_users_login_id;

-- 3) 칼럼 제거
ALTER TABLE public.users DROP COLUMN IF EXISTS login_id;
