-- Deletes all of the Images associated with a store from the Database
-- ===========================================================================
DELETE
FROM Store_Images
WHERE store_id = ?
