-- Find Stores around a Geolocation
-- Returns resulting stores and the distance from the specified GeoLocation
-- ===========================================================================

SELECT
	*,
	ST_Distance(location, ST_SetSRID(ST_Point(?, ?), 4326)::geography) AS distance_meters
FROM Stores
WHERE ST_DWithin(location, ST_SetSRID(ST_Point(?, ?), 4326)::geography, ?)
ORDER BY distance_meters
