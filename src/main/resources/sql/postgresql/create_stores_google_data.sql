-- Used to store information from Google about EBT Stores
-- ===========================================================================

CREATE TABLE IF NOT EXISTS Stores_Google_Data
(
		store_id uuid PRIMARY KEY REFERENCES Stores(store_id),
		place_id text NOT NULL,
		name TEXT,
		rating NUMERIC,
		vicinity TEXT,
		address_components JSON,
		formatted_address TEXT,
		formatted_phone_number TEXT,
		international_phone_number TEXT,
		is_permanently_closed BOOLEAN,
		latitude NUMERIC,
		longitude NUMERIC,
		icon TEXT,
		url TEXT,
		website TEXT,
		utc_offset NUMERIC,
		-- Array of Reviews for the Store
		reviews JSON,
		types TEXT[],
		scope TEXT
);
