-- Used to store information from Google about EBT Stores
-- ===========================================================================

CREATE TABLE IF NOT EXISTS Stores
(
		store_id uuid PRIMARY KEY REFERENCES Stores(store_id),
		place_id text UNIQUE NOT NULL,
		name TEXT,
		rating NUMBER,
		vicinity TEXT,
		address_components JSON,
		formatted_address TEXT,
		formatted_phone_number TEXT,
		international_phone_number TEXT,
		is_permanently_closed BOOLEAN,
		latitude NUMBER,
		longitude NUMBER,
		icon TEXT,
		url TEXT,
		website TEXT,
		utc_offset NUMBER,
		-- Array of Reviews for the Store
		reviews JSON,
		types TEXT[],
		scope TEXT,

		PRIMARY KEY(store_id)
);
