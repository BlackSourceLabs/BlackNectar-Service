CREATE TABLE IF NOT EXISTS Stores
(
	store_id uuid PRIMARY KEY,
    store_name VARCHAR(100),
    latitude NUMERIC,
    longitude NUMERIC,
    location GEOGRAPHY,
    address VARCHAR(100),
    address_line_two VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(5),
    county VARCHAR(100),
    zip_code INTEGER,
    local_zip_code INTEGER,

    CONSTRAINT Unique_Stores UNIQUE(store_name, latitude, longitude)
);
