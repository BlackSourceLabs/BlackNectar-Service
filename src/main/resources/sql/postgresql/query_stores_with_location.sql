-- Find Stores around a Geolocation
-- Returns resulting stores and the distance from the specified GeoLocation
-- ===========================================================================

SELECT
	Stores.*,
	Store_Images.url,
	ST_Distance(location, ST_SetSRID(ST_Point(?, ?), 4326)::geography) AS distance_meters
FROM Stores
LEFT JOIN Store_Cover_Images USING(store_id)
LEFT JOIN Store_Images USING(store_id, image_id)
WHERE ST_DWithin(location, ST_SetSRID(ST_Point(?, ?), 4326)::geography, ?)
ORDER BY distance_meters ASC
