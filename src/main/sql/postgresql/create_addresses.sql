-- It is good practice to normalize data, but for the purposes of BlackNectar,
-- we do not really need a separate normalized address table.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS Store_Addresses
(
	address_id uuid PRIMARY KEY,
	address_line_one VARCHAR(100) NOT NULL,
	address_line_two VARCHAR(100),
	city VARCHAR(100),
	state VARCHAR(5),
	county VARCHAR(100),
	zip_code INTEGER,
	local_zip_code INTEGER,

	CONSTRAINT Unique_Addresses UNIQUE(address_line_one, address_line_two, city, state, county, zip_code)
);
