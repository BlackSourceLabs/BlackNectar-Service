-- Used to store images associated with a Store
-- ===========================================================================

CREATE TABLE IF NOT EXISTS Store_Images
(
		store_id uuid REFERENCES Stores(store_id),
		image_id text,
		-- Which type of image is it? Cover Image? Alternative Image? Food Image? Etc.
		image_type text,

		PRIMARY KEY (store_id, image_id)
);
