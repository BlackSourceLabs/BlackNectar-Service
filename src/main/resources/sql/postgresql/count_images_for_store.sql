-- Calculates the numer of images stored for a particular store
-- ===========================================================================
SELECT Count(*) AS count
FROM Store_Images
WHERE store_id = ?
