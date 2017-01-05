-- Finds a Particular image
-- Returns the Image, including the raw image_binary data.
-- ===========================================================================

SELECT store_id, image_id, image_binary, height, width, size_in_bytes, content_type, image_type, source, url
FROM blacknectar.store_images
WHERE store_id = ?
AND image_id = ?
