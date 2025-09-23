create table if not exists favorite_cart (
  user_id   bigint       not null,
  favorite  varchar(255) not null,
  created_at  timestamptz not null default now(),
  updated_at  timestamptz not null default now(),
  constraint pk_favorite_cart primary key (user_id, favorite),
  constraint fk_favorite_cart_user foreign key (user_id)
    references users(id) on delete cascade
);

create index if not exists idx_favorite_cart_user on favorite_cart(user_id);
create index if not exists idx_favorite_cart_fav  on favorite_cart(favorite);