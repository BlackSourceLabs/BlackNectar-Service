-- Used to store the main Cover image associated with a Store
-- ===========================================================================

CREATE TABLE IF NOT EXISTS Store_Cover_Images
(
		store_id uuid,
		image_id text NOT NULL,

		CONSTRAINT foreign_key FOREIGN KEY(store_id, image_id) REFERENCES Store_Images(store_id, image_id),

		PRIMARY KEY (store_id)
);
