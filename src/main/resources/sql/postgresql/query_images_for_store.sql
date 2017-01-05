-- Find Images for a particular store
-- Returns resulting images, if any, of the specified store
-- ===========================================================================

SELECT store_id, image_id, image_binary, height, width, size_in_bytes, content_type, image_type, source, url
FROM blacknectar.store_images
WHERE store_id = ?;
