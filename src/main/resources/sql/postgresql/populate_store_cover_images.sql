-- This script searches through the store_images table, picks one image for each store,
-- and loads it into the store_cover_images table.
-- ===========================================================================

WITH UniqueImage AS
(
	SELECT store_id, min(image_id) AS image_id
	FROM store_images
	GROUP BY store_id
)
,
OneImage AS
(
	SELECT *
	FROM UniqueImage
	INNER JOIN store_images
		USING (store_id, image_id)
)
INSERT INTO store_cover_images
SELECT store_id, image_id
FROM OneImage
;
