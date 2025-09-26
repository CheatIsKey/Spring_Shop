-- V12__delivery_drop_legacy_order_id.sql
-- 목적:
--  1) 과거 설계(Delivery.order_id FK) 데이터를 새 설계(Orders.delivery_id)로 백필
--  2) delivery.order_id 관련 제약/컬럼 제거

-- 0) 안전장치: orders.delivery_id 컬럼 없으면 추가
ALTER TABLE public.orders
    ADD COLUMN IF NOT EXISTS delivery_id BIGINT;

-- 1) 백필: (delivery.order_id -> orders.id) 매핑으로 orders.delivery_id 채움
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'delivery'
          AND column_name = 'order_id'
    ) THEN
        UPDATE public.orders o
        SET delivery_id = d.id
        FROM public.delivery d
        WHERE o.delivery_id IS NULL
          AND d.order_id = o.id;
    END IF;
END$$;

-- 2) delivery.order_id를 참조하는 모든 제약(FK/UNIQUE 등) 삭제
DO $$
DECLARE
    r record;
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'delivery'
          AND column_name = 'order_id'
    ) THEN
        FOR r IN
            SELECT con.conname
            FROM pg_constraint con
            JOIN pg_class rel       ON rel.oid = con.conrelid
            JOIN pg_namespace nsp   ON nsp.oid = rel.relnamespace
            JOIN LATERAL unnest(con.conkey) WITH ORDINALITY AS cols(attnum, ord) ON true
            JOIN pg_attribute a     ON a.attrelid = rel.oid AND a.attnum = cols.attnum
            WHERE nsp.nspname = 'public'
              AND rel.relname = 'delivery'
              AND a.attname = 'order_id'
        LOOP
            EXECUTE format('ALTER TABLE public.delivery DROP CONSTRAINT %I', r.conname);
        END LOOP;
    END IF;
END$$;

-- 3) 최종: delivery.order_id 컬럼 제거
ALTER TABLE public.delivery
    DROP COLUMN IF EXISTS order_id;

-- 4) 보강: orders.delivery_id -> delivery.id FK(없으면 추가) + 인덱스
DO $$
BEGIN
    CREATE INDEX IF NOT EXISTS idx_orders_delivery_id ON public.orders(delivery_id);
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_orders_delivery'
    ) THEN
        ALTER TABLE public.orders
            ADD CONSTRAINT fk_orders_delivery
            FOREIGN KEY (delivery_id) REFERENCES public.delivery(id)
            ON DELETE SET NULL;
    END IF;
END$$;
