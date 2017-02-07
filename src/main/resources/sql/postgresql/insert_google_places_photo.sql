-- Inserts Google Photo Information.
-- ===========================================================================

INSERT INTO blacknectar.google_places_photos
(photo_reference, place_id, height, width, html_attributions, url)
VALUES (?, ?, ?, ?, string_to_array(?, ','), ?);
