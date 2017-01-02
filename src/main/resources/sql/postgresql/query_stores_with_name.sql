-- Find Stores that match a specific name.
-- Returns resulting stores that match the specified name, limited to 100.
-- ===========================================================================

SELECT
	*
FROM Stores
WHERE store_name LIKE ?
ORDER BY store_name
LIMIT 100
