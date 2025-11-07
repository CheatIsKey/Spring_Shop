-- V15__item_add_created_by.sql
ALTER TABLE public.item
  ADD COLUMN IF NOT EXISTS created_by_id BIGINT;

ALTER TABLE public.item
  ADD CONSTRAINT IF NOT EXISTS fk_item_created_by
  FOREIGN KEY (created_by_id) REFERENCES public.users(id);

CREATE INDEX IF NOT EXISTS idx_item_created_by_id
  ON public.item(created_by_id);