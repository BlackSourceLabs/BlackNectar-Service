-- Deletes an Image from the Database
-- ===========================================================================
DELETE
FROM Store_Images
WHERE store_id = ?
AND image_id = ?
