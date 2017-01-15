-- Returns 1 if the specified store_id exists, 0 if it does not exist.
-- ===========================================================================
SELECT Count(*) AS count
FROM Stores
WHERE store_id = ?
