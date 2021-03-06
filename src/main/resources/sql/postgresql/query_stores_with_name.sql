-- Find Stores that match a specific name.
-- Returns resulting stores that match the specified name.
-- ===========================================================================

SELECT
	Stores.*,
	Store_Images.url
FROM Stores
LEFT JOIN Store_Cover_Images USING(store_id)
LEFT JOIN Store_Images USING(store_id, image_id)
WHERE store_name LIKE ?
ORDER BY store_name
