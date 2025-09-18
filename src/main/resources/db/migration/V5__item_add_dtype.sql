ALTER TABLE public.item
  ADD COLUMN IF NOT EXISTS dtype VARCHAR(31);

UPDATE public.item SET dtype = 'Item' WHERE dtype IS NULL;

ALTER TABLE public.item
  ALTER COLUMN dtype SET NOT NULL;
