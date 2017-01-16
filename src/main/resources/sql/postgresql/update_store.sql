-- Updates an existing Store with new information
-- ===========================================================================
UPDATE Stores
SET store_id=?,
    store_name=?,
    store_code=?,
    latitude=?,
    longitude=?,
    location=public.ST_SetSRID(public.ST_Point(?, ?), 4326)::public.geography,
    address_line_one=?,
    address_line_two=?,
    city=?,
    state=?,
    county=?,
    zip_code=?,
    local_zip_code=?
WHERE store_id = ?
