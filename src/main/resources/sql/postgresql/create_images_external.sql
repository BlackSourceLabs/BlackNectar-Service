-- Used to store images from other places around the internet
-- ===========================================================================

CREATE TABLE IF NOT EXISTS Images_External
(
	image_id text PRIMARY KEY,
	-- This is otherwise known as the image blob
	image_binary BYTEA,
	height INT,
	width INT,
	size_in_bytes INT,
	content_type TEXT,
	image_type TEXT,
	source TEXT,
	url TEXT
);
