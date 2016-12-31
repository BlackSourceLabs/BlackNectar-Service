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
    zip_code text,
    local_zip_code text,

    PRIMARY KEY(store_name, latitude, longitude)
);
