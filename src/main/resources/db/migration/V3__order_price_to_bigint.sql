-- order_item.order_price 를 BIGINT 로 변경
ALTER TABLE order_item
  ALTER COLUMN order_price TYPE BIGINT USING order_price::BIGINT;
