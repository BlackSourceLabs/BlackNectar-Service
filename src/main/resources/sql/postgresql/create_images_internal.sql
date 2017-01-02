-- Used to store images owned by us.
-- Typically these are images that have been uploaded by our users.
-- ===========================================================================

CREATE TABLE IF NOT EXISTS Images
(
		image_id text PRIMARY KEY DEFAULT gen_random_uuid(),
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
