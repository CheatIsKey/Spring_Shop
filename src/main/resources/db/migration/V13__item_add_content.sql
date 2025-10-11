-- 상품 본문 컬럼 추가 (NULL 허용, TEXT)
ALTER TABLE public.item
    ADD COLUMN IF NOT EXISTS content TEXT;

-- (선택) 폼 검증과 일치시키고 싶으면 DB 레벨 길이 제약도 걸어줍니다.
ALTER TABLE public.item
    DROP CONSTRAINT IF EXISTS item_content_length;

ALTER TABLE public.item
    ADD CONSTRAINT item_content_length
    CHECK (content IS NULL OR length(content) <= 20000);
