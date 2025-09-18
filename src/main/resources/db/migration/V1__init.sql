-- =========================
-- V1__init.sql  (PostgreSQL)
-- =========================

-- 0) 확장/세션(선택)
-- CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 1) USERS
create table if not exists users (
  id          bigserial primary key,
  name        varchar(100),
  phone       varchar(50),
  id_user     varchar(50)  not null,
  password    varchar(255),

  -- Embedded Address (User)
  state       varchar(100),
  city        varchar(100),
  street      varchar(200),

  role        varchar(20),
  constraint uq_users_id_user unique (id_user),
  constraint ck_users_role check (role in ('USER','ADMIN'))
);

-- 2) DELIVERY (비소유측; FK는 orders.delivery_id가 가짐)
create table if not exists delivery (
  id     bigserial primary key,

  -- Embedded Address (Delivery)
  state  varchar(100) not null,
  city   varchar(100) not null,
  street varchar(200) not null,

  status varchar(20)  not null,
  constraint ck_delivery_status check (status in ('READY','COMP','CANCEL'))
);

-- 3) ITEM (다른 패키지 capstone.capstone_shop.domain.item.Item)
create table if not exists item (
  id        bigserial primary key,
  name      varchar(200) not null,
  price     integer      not null,
  stock     integer      not null,
  image_url text,
  constraint ck_item_price_nonneg check (price >= 0),
  constraint ck_item_stock_nonneg check (stock >= 0)
);

-- 4) CATEGORY (자기참조 트리)
create table if not exists category (
  id        bigserial primary key,
  name      varchar(100) not null,
  parent_id bigint,
  constraint fk_category_parent
    foreign key (parent_id) references category(id)
    on delete set null
);

-- 5) ORDERS (소유: user_id, delivery_id)
create table if not exists orders (
  id          bigserial primary key,
  user_id     bigint      not null,
  delivery_id bigint      not null,
  status      varchar(20) not null,
  order_date  timestamp   not null default now(),

  constraint fk_orders_user
    foreign key (user_id) references users(id) on delete restrict,

  constraint fk_orders_delivery
    foreign key (delivery_id) references delivery(id) on delete restrict,

  constraint uq_orders_delivery unique (delivery_id),
  constraint ck_orders_status check (status in ('ORDER','CANCEL'))
);

-- 6) ORDER_ITEM
create table if not exists order_item (
  id           bigserial primary key,
  order_id     bigint      not null,
  item_id      bigint      not null,
  order_price  integer     not null,
  count        integer     not null,

  constraint fk_order_item_order
    foreign key (order_id) references orders(id) on delete cascade,

  constraint fk_order_item_item
    foreign key (item_id)  references item(id)   on delete restrict,

  constraint ck_order_item_price_nonneg check (order_price >= 0),
  constraint ck_order_item_count_pos   check (count > 0)
);

-- 7) CATEGORY_ITEM (조인 엔티티, 단일 PK + 중복 방지)
create table if not exists category_item (
  id          bigserial primary key,
  category_id bigint not null,
  item_id     bigint not null,

  constraint fk_category_item_category
    foreign key (category_id) references category(id) on delete cascade,
  constraint fk_category_item_item
    foreign key (item_id)     references item(id)     on delete cascade,

  constraint uq_category_item unique (category_id, item_id)
);

-- 8) FAVORITE_CART (@ElementCollection Set<String> favorite)
--   테이블명은 하이버네이트가 찾는 소문자 favorite_cart 로 생성
create table if not exists favorite_cart (
  user_id  bigint      not null,
  favorite varchar(255) not null,
  constraint pk_favorite_cart primary key (user_id, favorite),
  constraint fk_favorite_cart_user foreign key (user_id)
    references users(id) on delete cascade
);

-- =========================
-- 인덱스 (권장)
-- =========================
-- 사용자 주문 조회
create index if not exists idx_orders_user_date
  on orders (user_id, order_date desc);

-- 주문 상세 조회
create index if not exists idx_order_item_order
  on order_item (order_id);

-- 카테고리 트리 탐색
create index if not exists idx_category_parent
  on category (parent_id);

-- 상품명 검색(선택)
create index if not exists idx_item_name_like
  on item (name);

-- 풀텍스트(선택)
-- create index if not exists idx_item_name_tsv
--   on item using gin (to_tsvector('simple', name));
