create table users
(
    id         uuid primary key,
    email      varchar(255) not null,
    created_at timestamptz  not null default now(),
    constraint uk_users_email unique (email)
);

create table locations
(
    id         uuid primary key,
    name       varchar(255) not null,
    latitude   double precision not null,
    longitude  double precision not null,
    created_at timestamptz  not null default now(),
    constraint uk_locations_lat_lon unique (latitude, longitude)
);

create table subscriptions
(
    id          uuid primary key,
    user_id     uuid        not null references users (id) on delete cascade,
    location_id uuid        not null references locations (id) on delete cascade,
    rule_type   varchar(32) not null,
    threshold   double precision not null,
    enabled     boolean     not null default true,
    created_at  timestamptz not null default now(),
    constraint uk_subscriptions_user_location_rule_threshold unique (user_id, location_id, rule_type, threshold)
);

create index idx_subscriptions_location_enabled on subscriptions (location_id, enabled);

create table weather_snapshots
(
    id               uuid primary key,
    location_id      uuid        not null references locations (id) on delete cascade,
    observed_at      timestamptz not null,
    temperature_c    double precision,
    precipitation_mm double precision,
    source           varchar(64) not null,
    created_at       timestamptz not null default now(),
    constraint uk_weather_location_observed_source unique (location_id, observed_at, source)
);

create index idx_weather_location_observed_desc on weather_snapshots (location_id, observed_at desc);

create table alert_events
(
    id              uuid primary key,
    subscription_id uuid        not null references subscriptions (id) on delete cascade,
    snapshot_id     uuid        not null references weather_snapshots (id) on delete cascade,
    status          varchar(16) not null,
    message         varchar(500) not null,
    created_at      timestamptz not null default now(),
    sent_at         timestamptz,
    constraint uk_alert_subscription_snapshot unique (subscription_id, snapshot_id)
);

create index idx_alert_status_created on alert_events (status, created_at);

