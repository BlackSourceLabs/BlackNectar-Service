-- Used to store Stores that support EBT
-- ===========================================================================

CREATE TABLE IF NOT EXISTS Stores
(
		store_id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    store_name TEXT,
		store_code TEXT,
    latitude NUMERIC NOT NULL,
    longitude NUMERIC NOT NULL,
    location GEOGRAPHY,
    address_line_one TEXT
    address_line_two TEXT,
    city TEXT,
    state TEXT,
    county TEXT,
    zip_code TEXT,
    local_zip_code TEXT,

    CONSTRAINT Unique_Stores UNIQUE(store_name, latitude, longitude)
);
