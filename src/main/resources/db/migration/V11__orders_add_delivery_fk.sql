-- V11__orders_add_delivery_fk.sql
-- 목적: orders.delivery_id FK 추가 (존재 안 하면 생성)
-- 안전장치: 컬럼/인덱스/제약조건 모두 IF NOT EXISTS로 idempotent 하게 처리

-- 1) delivery 테이블이 없는 환경에서도 안전하게 동작하도록 보강 (이미 있으면 스킵)
CREATE TABLE IF NOT EXISTS public.delivery (
    id BIGSERIAL PRIMARY KEY,
    state VARCHAR(255),
    city VARCHAR(255),
    street VARCHAR(255),
    status VARCHAR(32)
);

-- 2) orders.delivery_id 컬럼 추가 (nullable; 과거 주문 레코드 보호)
ALTER TABLE public.orders
    ADD COLUMN IF NOT EXISTS delivery_id BIGINT;

-- 3) 인덱스 (조인 성능)
CREATE INDEX IF NOT EXISTS idx_orders_delivery_id ON public.orders(delivery_id);

-- 4) FK 제약 (배송 레코드가 지워져도 주문은 남게 SET NULL)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_orders_delivery'
    ) THEN
        ALTER TABLE public.orders
            ADD CONSTRAINT fk_orders_delivery
            FOREIGN KEY (delivery_id) REFERENCES public.delivery(id)
            ON DELETE SET NULL;
    END IF;
END$$;
