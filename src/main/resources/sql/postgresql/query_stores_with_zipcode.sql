-- Find Stores that match zip code
-- Returns resulting stores that match the specified zip code.
-- ===========================================================================

SELECT
	Stores.*,
	Store_Images.url
FROM Stores
LEFT JOIN Store_Cover_Images USING(store_id)
LEFT JOIN Store_Images USING(store_id, image_id)
WHERE zip_code = ?
ORDER BY store_name ASC
