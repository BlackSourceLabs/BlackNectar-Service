-- Find Stores around that match a name in a given location.
-- Returns stores that match the specified name and the distance.
-- ===========================================================================

SELECT
	Stores.*,
	Store_Images.url,
	ST_Distance(location, ST_SetSRID(ST_Point(?, ?), 4326)::geography) AS distance_meters
FROM Stores
LEFT JOIN Store_Cover_Images USING(store_id)
LEFT JOIN Store_Images USING(store_id, image_id)
WHERE store_name LIKE ?
AND ST_DWithin(location, ST_SetSRID(ST_Point(?, ?), 4326)::geography, ?)
ORDER BY distance_meters
