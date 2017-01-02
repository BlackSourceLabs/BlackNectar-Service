-- Find Stores around that match a name in a given location.
-- Returns stores that match the specified name and the distance.
-- ===========================================================================

SELECT
	*,
	ST_Distance(location, ST_SetSRID(ST_Point(?, ?), 4326)::geography) AS distance_meters
FROM Stores
WHERE store_name LIKE ?
AND ST_DWithin(location, ST_SetSRID(ST_Point(?, ?), 4326)::geography, ?)
ORDER BY distance_meters
