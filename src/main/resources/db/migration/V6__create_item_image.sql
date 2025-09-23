CREATE TABLE IF NOT EXISTS public.item_image (
    id         BIGSERIAL PRIMARY KEY,
    item_id    BIGINT NOT NULL REFERENCES public.item(id) ON DELETE CASCADE,
    url        VARCHAR(1024) NOT NULL,
    file_name  VARCHAR(255),
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_item_image_item ON public.item_image(item_id);
CREATE INDEX IF NOT EXISTS idx_item_image_sort ON public.item_image(sort_order);
