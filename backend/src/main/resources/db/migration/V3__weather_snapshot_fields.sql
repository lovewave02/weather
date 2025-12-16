alter table weather_snapshots
    add column apparent_temperature_c double precision;

alter table weather_snapshots
    add column weather_code integer;

