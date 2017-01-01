INSERT INTO Stores(store_id, store_name, latitude, longitude, location, address_line_one, address_line_two, city, state, county, zip_code, local_zip_code)
VALUES (?, ?, ?, ?, public.ST_SetSRID(public.ST_Point(?, ?), 4326)::public.geography, ?, ?, ?, ?, ?, ?, ?)
