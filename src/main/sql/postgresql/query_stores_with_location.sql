-- Find Stores around a Geolocation
-- Returns resulting stores and the distance from the specified GeoLocation
-- ===========================================================================

SELECT
	*,
	ST_Distance(location, ST_SetSRID(ST_Point(?, ?), 4326)) AS distance_meters
FROM BlackNectar.Stores
WHERE ST_DWithin(location, ST_SetSRID(ST_Point(?, ?), 4326), ?)
ORDER BY distance
