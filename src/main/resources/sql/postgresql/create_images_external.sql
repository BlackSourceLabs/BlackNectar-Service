-- Used to store images from other places around the internet
-- ===========================================================================

CREATE TABLE IF NOT EXISTS BlackNectar.Images_External
(
		url TEXT PRIMARY KEY,
		binary BYTEA,
		height INT,
		width INT,
		size_in_bytes INT,
		file_type TEXT
);
