-- Inserts information from Google Places.
-- ===========================================================================

INSERT INTO stores_google_data
(store_id, place_id, name, rating, vicinity, address_components, formatted_address, formatted_phone_number, international_phone_number, is_permanently_closed, latitude, longitude, icon, url, website, utc_offset, reviews, types, scope)
VALUES (?, ?, ?, ?, ?, to_json(?::json), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_json(?::json), string_to_array(?, ','), ?)
