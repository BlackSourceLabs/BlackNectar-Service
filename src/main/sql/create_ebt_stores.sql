CREATE TABLE IF NOT EXISTS Stores
(
    store_name text,
    latitude numeric,
    longitude numeric,
    address text,
    address_line_two text,
    city text,
    state text,
    county text,
    zip text,
    local_zip text,

    PRIMARY KEY(store_name, latitude, longitude)
);
