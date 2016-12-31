-- Used to store Stores that support EBT
-- ===========================================================================

CREATE TABLE IF NOT EXISTS BlackNectar.Stores
(
		store_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    store_name VARCHAR(100),
    latitude NUMERIC NOT NULL,
    longitude NUMERIC NOT NULL,
    location GEOGRAPHY,
    address_line_one VARCHAR(100),
    address_line_two VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(5),
    county VARCHAR(100),
    zip_code VARCHAR(20),
    local_zip_code VARCHAR(20),

    CONSTRAINT Unique_Stores UNIQUE(store_name, latitude, longitude)
);
