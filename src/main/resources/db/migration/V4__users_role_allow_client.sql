-- 기존 체크 제약조건 제거
ALTER TABLE public.users
  DROP CONSTRAINT IF EXISTS ck_users_role;

ALTER TABLE public.users
  ADD CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'CLIENT'));
