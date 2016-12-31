INSERT INTO BlackNectar.Stores(store_id, store_name, latitude, longitude, location, address_line_one, address_line_two, city, state, county, zip_code, local_zip_code)
VALUES (?, ?, ?, ?, ST_SetSRID(ST_Point(?, ?), 4326)::geography, ?, ?, ?, ?, ?, ?, ?)
