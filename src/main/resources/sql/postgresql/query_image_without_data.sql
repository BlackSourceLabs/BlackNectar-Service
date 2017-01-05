-- Finds a Particular image
-- Returns resulting images of the specified store, but without the image_binary data.
-- This is done to save space in the case where only the image metadata is wanted.
-- ===========================================================================

SELECT store_id, image_id, height, width, size_in_bytes, content_type, image_type, source, url
FROM blacknectar.store_images
WHERE store_id = ?
AND image_id = ?
