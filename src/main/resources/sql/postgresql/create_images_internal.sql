-- Used to store images owned by us.
-- Typically these are images that have been uploaded by our users.
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
