-- Updates an existing Store with new information
-- ===========================================================================
UPDATE Stores
SET store_id=?, store_name=?, latitude=?, longitude=?, location=?, address_line_one=?, address_line_two=?, city=?, state=?, county=?, zip_code=?, local_zip_code=?, store_code=?
WHERE store_id = ?
